package jp.hisano.wasm.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Module {
	private final List<FunctionType> functionTypes = new ArrayList<>();
	private final List<Function> functions = new ArrayList<>();
	private final Map<String, ExportedFunction> exportedFunctions = new HashMap<>();

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

	enum ValueType {
		I32, I64, F32, F64
	}

	class Function {
		final ValueType[] parameterTypes;
		final ValueType[] returnTypes;

		ValueType[] localTypes;
		FunctionBlock functionBlock;

		Function(ValueType[] parameterTypes, ValueType[] returnTypes) {
			this.parameterTypes = parameterTypes;
			this.returnTypes = returnTypes;
		}

		void setBody(ValueType[] localTypes, FunctionBlock functionBlock) {
			this.localTypes = localTypes;
			this.functionBlock = functionBlock;
		}

		public void execute(Frame frame) {
			functionBlock.execute(frame);
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

	static class End implements Instruction {
		@Override
		public void execute(Frame frame) {
		}
	}

	static class LocalGet implements Instruction {
		private final int index;

		LocalGet(int index) {
			this.index = index;
		}

		@Override
		public void execute(Frame frame) {
			frame.push(frame.getLocalVariable(index));
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

	static class I32Add extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first + second;
		}
	}

	static class I32Sub extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first - second;
		}
	}

	static class I32Mul extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first * second;
		}
	}

	static class I32DivS extends TwoOperandNumericOperator {
		@Override
		int calculate(int first, int second) {
			return first / second;
		}
	}

	static class I32Xor extends TwoOperandNumericOperator {
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

	static class I32Extend8S extends NumericConverter {
		@Override
		int convert(int value) {
			return (byte)value;
		}
	}

	static class I32Extend16S extends NumericConverter {
		@Override
		int convert(int value) {
			return (short)value;
		}
	}

	static abstract class Block implements Instruction {
		private Block parent;

		Block(Block parent) {
			this.parent = parent;
		}
	}

	static abstract class SimpleBlock extends Block {
		protected List<Instruction> instructions;

		SimpleBlock(Block parent) {
			super(parent);
		}

		void setInstructions(List<Instruction> instructions) {
			this.instructions = instructions;
		}
	}

	static class FunctionBlock extends SimpleBlock {
		private final Function function;

		FunctionBlock(Function function) {
			super(null);
			this.function = function;
		}

		@Override
		public void execute(Frame frame) {
			instructions.forEach(instruction -> instruction.execute(frame));
		}
	}
}
