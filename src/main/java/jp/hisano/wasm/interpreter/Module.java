package jp.hisano.wasm.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.*;
import jp.hisano.wasm.interpreter.Frame.ExceptionToExitBlock;
import jp.hisano.wasm.interpreter.Frame.ExceptionToReturn;
import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;

final class Module {
	private final List<FunctionType> functionTypes = new ArrayList<>();
	private final List<Function> functions = new ArrayList<>();
	private final Map<String, ExportedFunction> exportedFunctions = new HashMap<>();

	private final List<GlobalVariable> globalVariables = new ArrayList<>();

	void addFunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
		functionTypes.add(new FunctionType(parameterTypes, returnTypes));
	}

	void addFunction(int typeIndex) {
		FunctionType functionType = functionTypes.get(typeIndex);
		functions.add(new Function(functionType.parameterTypes, functionType.returnTypes));
	}

	void addExportedFunction(String name, int functionIndex) {
		exportedFunctions.put(name, new ExportedFunction(this, functions.get(functionIndex)));
	}

	Function getFunction(int functionIndex) {
		return functions.get(functionIndex);
	}

	ExportedFunction getExportedFunction(String name) {
		return exportedFunctions.get(name);
	}

	void addGlobalVariable(ValueType type, boolean isMutable, List<Instruction> instructions) {
		globalVariables.add(new GlobalVariable(globalVariables.size() - 1, type, isMutable, instructions));
	}

	public void prepareGlobalVariables() {
		globalVariables.stream().forEach(globalVariable -> {
			if (globalVariable.getValueType() == ValueType.I32) {
				// モジュールでインポートされたGlobal
				if (globalVariable.instructions.isEmpty()) {
					return;
				}

				Frame frame = new Frame(this, null);
				globalVariable.instructions.forEach(instruction -> {
					instruction.execute(frame);
				});
				globalVariable.setI32(frame.pop());
			}
		});
	}

	enum Kind {
		FUNCTION, TABLE, MEMORY, GLOBAL,
	}

	enum ValueType {
		VOID, I32, I64, F32, F64, V128, FUNCREF, EXTERNREF,
	}

	static class Variable {
		private final ValueType valueType;

		private int i32Value;
		private long i64Value;
		private float f32Value;
		private double f64Value;

		Variable(ValueType valueType) {
			this.valueType = valueType;
		}

		ValueType getValueType() {
			return valueType;
		}

		void setI32(int newValue) {
			i32Value = newValue;
		}

		int getI32() {
			return i32Value;
		}
	}

	static class GlobalVariable extends Variable {
		private final int index;
		private final boolean isMutable;
		private final List<Instruction> instructions;

		GlobalVariable(int index, ValueType valueType, boolean isMutable, List<Instruction> instructions) {
			super(valueType);
			this.index = index;
			this.isMutable = isMutable;
			this.instructions = instructions;
		}

		@Override
		public String toString() {
			return "GlobalVariable{" +
				"index=" + index +
				", valueType=" + getValueType() +
				", isMutable=" + isMutable + 
				", instructions.length = " + instructions.size() +
				'}';
		}
	}

	class Function {
		final ValueType[] parameterTypes;
		final ValueType[] returnTypes;

		ValueType[] localTypes;
		byte[] instructions;
		FunctionBlock functionBlock;

		Function(ValueType[] parameterTypes, ValueType[] returnTypes) {
			this.parameterTypes = parameterTypes;
			this.returnTypes = returnTypes;
		}

		void setBody(ValueType[] localTypes, byte[] instructions) {
			this.localTypes = localTypes;
			this.instructions = instructions;
		}

		void execute(Frame frame) {
			if (functionBlock == null) {
				functionBlock = new Parser(instructions).parseFunctionBlock(frame.getModule(), this);
			}
			functionBlock.execute(frame);
		}

		void executeWithNewFrame(Frame parent) {
			Frame frame = new Frame(parent.getModule(), this);

			for (int i = parameterTypes.length - 1; 0 <= i; i--) {
				switch (parameterTypes[i]) {
					case I32:
						frame.getLocalVariable(i).setI32(parent.pop());
						break;
				}
			}

			execute(frame);

			if (returnTypes.length == 0) {
				return;
			}

			switch (returnTypes[0]) {
				case I32:
					parent.push(frame.pop());
			}
		}
	}

	private class FunctionType {
		private final ValueType[] parameterTypes;
		private final ValueType[] returnTypes;

		FunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
			this.parameterTypes = parameterTypes;
			this.returnTypes = returnTypes;
		}
	}

	interface Instruction {
		void execute(Frame frame);
	}

	final static class Unreachable implements Instruction {
		@Override
		public void execute(Frame frame) {
			throw new InterpreterException(UNREACHABLE);
		}
	}

	abstract static class BlockEndMarker implements Instruction {
		@Override
		public void execute(Frame frame) {
		}
	} 

	final static class End extends BlockEndMarker {
	}

	final static class Return extends ExitBlock {
		@Override
		public void execute(Frame frame) {
			frame.throwExceptionToReturn();
		}
	}

	final static class Call implements Instruction {
		private final Function function;

		Call(Function function) {
			this.function = function;
		}

		@Override
		public void execute(Frame frame) {
			function.executeWithNewFrame(frame);
		}
	}

	private static abstract class ExitBlock implements Instruction {
	}

	final static class Br extends ExitBlock {
		private final int depth;

		Br(int depth) {
			this.depth = depth;
		}

		@Override
		public void execute(Frame frame) {
			frame.throwExceptionToExitBlock(depth);
		}
	}

	final static class BrIf extends ExitBlock {
		private final int depth;

		BrIf(int depth) {
			this.depth = depth;
		}

		@Override
		public void execute(Frame frame) {
			if (frame.pop() != 0) {
				frame.throwExceptionToExitBlock(depth);
			}
		}
	}

	final static class BrTable extends ExitBlock {
		private final int[] depths;
		private final int defaultDepth;

		BrTable(int[] depths, int defaultDepth) {
			this.depths = depths;
			this.defaultDepth = defaultDepth;
		}

		@Override
		public void execute(Frame frame) {
			int index = frame.pop();
			if (index < depths.length) {
				frame.throwExceptionToExitBlock(depths[index]);
			} else {
				frame.throwExceptionToExitBlock(defaultDepth);
			}
		}
	}

	final static class Drop implements Instruction {
		@Override
		public void execute(Frame frame) {
			frame.pop();
		}
	}

	final static class LocalGet implements Instruction {
		private final int index;

		LocalGet(int index) {
			this.index = index;
		}

		@Override
		public void execute(Frame frame) {
			frame.push(frame.getLocalVariable(index).getI32());
		}
	}

	final static class GlobalGet implements Instruction {
		private final int index;

		GlobalGet(int index) {
			this.index = index;
		}

		@Override
		public void execute(Frame frame) {
			frame.push(frame.getModule().getGlobalVariable(index).getI32());
		}
	}

	private GlobalVariable getGlobalVariable(int index) {
		return globalVariables.get(index);
	}

	final static class I32Const implements Instruction {
		private final int value;

		I32Const(int value) {
			this.value = value;
		}

		@Override
		public void execute(Frame frame) {
			frame.push(value);
		}
	}

	final static class I64Const implements Instruction {
		private final long value;

		I64Const(long value) {
			this.value = value;
		}

		@Override
		public void execute(Frame frame) {
			throw new UnsupportedOperationException();
		}
	}

	final static class F32Const implements Instruction {
		private final float value;

		F32Const(float value) {
			this.value = value;
		}

		@Override
		public void execute(Frame frame) {
			throw new UnsupportedOperationException();
		}
	}

	final static class F64Const implements Instruction {
		private final double value;

		F64Const(double value) {
			this.value = value;
		}

		@Override
		public void execute(Frame frame) {
			throw new UnsupportedOperationException();
		}
	}

	private static abstract class TwoOperandNumericOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			int second = frame.pop();
			int first = frame.pop();
			frame.push(calculate(first, second));
		}

		abstract int calculate(int first, int second);
	}

	final static class I32Ctz extends NumericConverter {
		@Override
		int convert(int value) {
			return numberOfTrailingZeros(value);
		}
	}

	final static class I32Add extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first + second;
		}
	}

	final static class I32Sub extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first - second;
		}
	}

	final static class I32Mul extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first * second;
		}
	}

	final static class I32DivS extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first / second;
		}
	}

	final static class I32Xor extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first ^ second;
		}
	}

	private static abstract class NumericConverter implements Instruction {
		@Override
		public void execute(Frame frame) {
			frame.push(convert(frame.pop()));
		}

		abstract int convert(int value);
	}

	final static class I32Extend8S extends NumericConverter {
		@Override
		int convert(int value) {
			return (byte)value;
		}
	}

	final static class I32Extend16S extends NumericConverter {
		@Override
		int convert(int value) {
			return (short)value;
		}
	}

	final static class RefNull implements Instruction {
		private final ValueType type;

		public RefNull(ValueType type) {
			this.type = type;
		}

		@Override
		public void execute(Frame frame) {
			throw new UnsupportedOperationException();
		}
	}

	static abstract class AbstractBlock implements Instruction {
		private AbstractBlock parent;

		AbstractBlock(AbstractBlock parent) {
			this.parent = parent;
		}
	}

	private static abstract class OneChildBlock extends AbstractBlock {
		private final boolean isBackwardExit;

		protected List<Instruction> instructions;

		OneChildBlock(AbstractBlock parent, boolean isBackwardExit) {
			super(parent);
			this.isBackwardExit = isBackwardExit;
		}

		void setInstructions(List<Instruction> instructions) {
			this.instructions = instructions;
		}

		@Override
		public void execute(Frame frame) {
			do {
				try {
					instructions.forEach(instruction -> {
						instruction.execute(frame);
					});
				} catch (ExceptionToExitBlock e) {
					if (e.isMoreExitRequired()) {
						e.decrementDepth();
						throw e;
					}
				}
			} while (isBackwardExit);
		}
	}

	final static class FunctionBlock extends OneChildBlock {
		private final Function function;

		FunctionBlock(Function function) {
			super(null, false);
			this.function = function;
		}

		@Override
		public void execute(Frame frame) {
			try {
				super.execute(frame);
			} catch (ExceptionToReturn e) {
			}
		}
	}

	final static class Block extends OneChildBlock {
		private final ValueType resultValueType;

		Block(AbstractBlock parent, ValueType resultValueType) {
			super(parent, false);
			this.resultValueType = resultValueType;
		}
	}

	final static class Loop extends OneChildBlock {
		private final ValueType resultValueType;

		Loop(AbstractBlock parent, ValueType resultValueType) {
			super(parent, true);
			this.resultValueType = resultValueType;
		}
	}

	final static class If extends AbstractBlock {
		private List<Instruction> thenInstructions;
		private List<Instruction> elseInstructions;

		private final ValueType resultValueType;

		If(AbstractBlock parent, ValueType resultValueType) {
			super(parent);
			this.resultValueType = resultValueType;
		}

		void setInstructions(List<Instruction> thenInstructions, List<Instruction> elseInstructions) {
			this.thenInstructions = thenInstructions;
			this.elseInstructions = elseInstructions;
		}

		@Override
		public void execute(Frame frame) {
			try {
				if (frame.pop() != 0) {
					thenInstructions.forEach(instruction -> {
						instruction.execute(frame);
					});
				} else {
					if (elseInstructions != null) {
						elseInstructions.forEach(instruction -> {
							instruction.execute(frame);
						});
					}
				}
			} catch (ExceptionToExitBlock e) {
				if (e.isMoreExitRequired()) {
					e.decrementDepth();
					throw e;
				}
			}
		}

	}

	final static class Else extends BlockEndMarker {
	}
}
