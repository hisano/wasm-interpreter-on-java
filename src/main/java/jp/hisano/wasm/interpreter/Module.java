package jp.hisano.wasm.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.*;
import jp.hisano.wasm.interpreter.Frame.ExceptionToExitBlock;
import jp.hisano.wasm.interpreter.Frame.ExceptionToReturn;
import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;

public final class Module {
	private final List<FunctionType> functionTypes = new ArrayList<>();
	private final List<Function> functions = new ArrayList<>();
	private final Map<String, ExportedFunction> exportedFunctions = new HashMap<>();

	private final List<GlobalVariableType> globalVariableTypes = new ArrayList<>();

	public Module(byte[] wasmFileContent) {
		new Parser(wasmFileContent).parseModule(this);
	}

	void addFunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
		functionTypes.add(new FunctionType(parameterTypes, returnTypes));
	}

	void addFunction(int typeIndex) {
		FunctionType functionType = functionTypes.get(typeIndex);
		functions.add(new Function(functionType.parameterTypes, functionType.returnTypes));
	}

	void addExportedFunction(String name, int functionIndex) {
		exportedFunctions.put(name, new ExportedFunction(functions.get(functionIndex)));
	}

	Function getFunction(int functionIndex) {
		return functions.get(functionIndex);
	}

	ExportedFunction getExportedFunction(String name) {
		return exportedFunctions.get(name);
	}

	void addGlobalVariableType(ValueType type, boolean isMutable, List<Instruction> instructions) {
		globalVariableTypes.add(new GlobalVariableType(globalVariableTypes.size() - 1, type, isMutable, instructions));
	}

	List<GlobalVariableType> getGlobalVariableTypes() {
		return globalVariableTypes;
	}

	enum Kind {
		FUNCTION, TABLE, MEMORY, GLOBAL,
	}

	enum ValueType {
		VOID, I32, I64, F32, F64, V128, FUNCREF, EXTERNREF,
	}

	static class VariableType {
		private final ValueType type;

		VariableType(ValueType type) {
			this.type = type;
		}

		ValueType getType() {
			return type;
		}
	}

	static class GlobalVariableType extends VariableType {
		private final int index;
		private final boolean isMutable;
		private final List<Instruction> instructions;

		GlobalVariableType(int index, ValueType type, boolean isMutable, List<Instruction> instructions) {
			super(type);
			this.index = index;
			this.isMutable = isMutable;
			this.instructions = instructions;
		}

		List<Instruction> getInstructions() {
			return instructions;
		}

		@Override
		public String toString() {
			return "GlobalVariable{" +
				"index=" + index +
				", valueType=" + getType() +
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

		void invoke(Frame frame) {
			if (functionBlock == null) {
				functionBlock = new Parser(instructions).parseFunctionBlock(frame.getInstance().getModule(), this);
			}
			functionBlock.execute(frame);
		}

		void executeWithNewFrame(Frame parent) {
			Frame frame = new Frame(parent.getInstance(), this);

			for (int i = parameterTypes.length - 1; 0 <= i; i--) {
				switch (parameterTypes[i]) {
					case I32:
						frame.getLocalVariable(i).getValue().setI32(parent.pop().getI32());
						break;
					case I64:
						frame.getLocalVariable(i).getValue().setI64(parent.pop().getI64());
						break;
				}
			}

			invoke(frame);

			if (returnTypes.length == 0) {
				return;
			}

			switch (returnTypes[0]) {
				case I32:
					parent.pushI32(frame.pop().getI32());
					break;
				case I64:
					parent.pushI64(frame.pop().getI64());
					break;
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
			if (frame.pop().getI32() != 0) {
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
			int index = frame.pop().getI32();
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

	private static abstract class PushValue implements Instruction {
		@Override
		public void execute(Frame frame) {
			Value value = getValue(frame);
			switch (value.getType()) {
				case I32:
					frame.pushI32(value.getI32());
					return;
				case I64:
					frame.pushI64(value.getI64());
					return;
				case F32:
					frame.pushF32(value.getF32());
					return;
				case F64:
					frame.pushF64(value.getF64());
					return;
			}
		}

		abstract Value getValue(Frame frame);
	}

	final static class LocalGet extends PushValue {
		private final int index;

		LocalGet(int index) {
			this.index = index;
		}

		@Override
		Value getValue(Frame frame) {
			return frame.getLocalVariable(index).getValue();
		}
	}

	final static class GlobalGet extends PushValue {
		private final int index;

		GlobalGet(int index) {
			this.index = index;
		}

		@Override
		Value getValue(Frame frame) {
			return frame.getInstance().getGlobalVariable(index).getValue();
		}
	}

	final static class I32Const implements Instruction {
		private final int value;

		I32Const(int value) {
			this.value = value;
		}

		@Override
		public void execute(Frame frame) {
			frame.pushI32(value);
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

	private static abstract class I32TwoOperandsOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			int second = frame.pop().getI32();
			int first = frame.pop().getI32();
			frame.pushI32(calculate(first, second));
		}

		abstract int calculate(int first, int second);
	}

	final static class I32Clz extends I32Converter {
		@Override
		int convert(int value) {
			return numberOfLeadingZeros(value);
		}
	}

	final static class I32Ctz extends I32Converter {
		@Override
		int convert(int value) {
			return numberOfTrailingZeros(value);
		}
	}

	final static class I32Add extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first + second;
		}
	}

	final static class I32Sub extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first - second;
		}
	}

	final static class I32Mul extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first * second;
		}
	}

	final static class I32DivS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first / second;
		}
	}

	final static class I32DivU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return divideUnsigned(first, second);
		}
	}

	final static class I32RemS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first % second;
		}
	}

	final static class I32RemU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return remainderUnsigned(first, second);
		}
	}

	final static class I32And extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first & second;
		}
	}

	final static class I32Or extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first | second;
		}
	}

	final static class I32Xor extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first ^ second;
		}
	}

	final static class I32Shl extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first << second;
		}
	}

	final static class I32ShrS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first >> second;
		}
	}

	final static class I32ShrU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first >>> second;
		}
	}

	final static class I32RotL extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return rotateLeft(first, second);
		}
	}

	final static class I32RotR extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return rotateRight(first, second);
		}
	}

	private static abstract class I32Converter implements Instruction {
		@Override
		public void execute(Frame frame) {
			frame.pushI32(convert(frame.pop().getI32()));
		}

		abstract int convert(int value);
	}

	final static class I32Extend8S extends I32Converter {
		@Override
		int convert(int value) {
			return (byte)value;
		}
	}

	final static class I32Extend16S extends I32Converter {
		@Override
		int convert(int value) {
			return (short)value;
		}
	}

	private static abstract class I64TwoOperandsOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			long second = frame.pop().getI64();
			long first = frame.pop().getI64();
			frame.pushI64(calculate(first, second));
		}

		abstract long calculate(long first, long second);
	}

	final static class I64Add extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first + second;
		}
	}

	private static abstract class F32TwoOperandsOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			float second = frame.pop().getF32();
			float first = frame.pop().getF32();
			frame.pushF32(calculate(first, second));
		}

		abstract float calculate(float first, float second);
	}

	final static class F32Add extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return first + second;
		}
	}

	private static abstract class F64TwoOperandsOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			double second = frame.pop().getF64();
			double first = frame.pop().getF64();
			frame.pushF64(calculate(first, second));
		}

		abstract double calculate(double first, double second);
	}

	final static class F64Add extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return first + second;
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
				if (frame.pop().getI32() != 0) {
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
