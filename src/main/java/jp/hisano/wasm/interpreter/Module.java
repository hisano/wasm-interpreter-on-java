package jp.hisano.wasm.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.lang.Integer.*;
import static java.lang.Long.divideUnsigned;
import static java.lang.Long.remainderUnsigned;
import static java.lang.Long.rotateLeft;
import static java.lang.Long.rotateRight;
import static java.lang.Math.*;
import jp.hisano.wasm.interpreter.Frame.ExceptionToExitBlock;
import jp.hisano.wasm.interpreter.Frame.ExceptionToReturn;
import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;

public final class Module {
	private final List<FunctionType> functionTypes = new ArrayList<>();
	private final List<Function> functions = new ArrayList<>();
	private final Map<String, ExportedFunction> exportedFunctions = new HashMap<>();

	private final List<TableType> tableTypes = new LinkedList<>();
	private final List<ElementType> elementTypes = new LinkedList<>();

	private final List<MemoryType> memoryTypes = new LinkedList<>();

	private final List<GlobalVariableType> globalVariableTypes = new LinkedList<>();

	public Module(byte[] wasmFileContent) {
		new Parser(wasmFileContent).parseModule(this);
	}

	void addFunctionType(ValueType[] parameterTypes, ValueType[] returnTypes) {
		functionTypes.add(new FunctionType(parameterTypes, returnTypes));
	}

	FunctionType getFunctionType(int index) {
		return functionTypes.get(index);
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

	void addTableType(ValueType elementType, int minimumElementLength, int maximumElementLength) {
		tableTypes.add(new TableType(elementType, minimumElementLength, maximumElementLength));
	}

	TableType getTableType(int index) {
		return tableTypes.get(index);
	}

	List<TableType> getTableTypes() {
		return tableTypes;
	}

	void addElementType(int tableIndex, List<Instruction> offsetInstructions, int[] elements) {
		elementTypes.add(new ElementType(tableIndex, offsetInstructions, elements));
	}

	List<ElementType> getElementTypes() {
		return elementTypes;
	}

	void addMemoryType(int minimumPageLength, int maximumPageLength) {
		memoryTypes.add(new MemoryType(minimumPageLength, maximumPageLength));
	}

	void addMemoryData(int memoryIndex, List<Instruction> offsetInstructions, byte[] data) {
		memoryTypes.get(memoryIndex).addData(offsetInstructions, data);
	}

	List<MemoryType> getMemoryTypes() {
		return memoryTypes;
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

	static class TableType {
		private final ValueType elementType;
		private final int minimumElementLength;
		private final int maximumElementLength;

		TableType(ValueType elementType, int minimumElementLength, int maximumElementLength) {
			this.elementType = elementType;
			this.minimumElementLength = minimumElementLength;
			this.maximumElementLength = maximumElementLength;
		}

		ValueType getElementType() {
			return elementType;
		}

		int getMinimumElementLength() {
			return minimumElementLength;
		}

		int getMaximumElementLength() {
			return maximumElementLength;
		}
	}

	static class ElementType {
		private final int tableIndex;
		private final List<Instruction> offsetInstructions;
		private final int[] elements;

		ElementType(int tableIndex, List<Instruction> offsetInstructions, int[] elements) {
			this.tableIndex = tableIndex;
			this.offsetInstructions = offsetInstructions;
			this.elements = elements;
		}

		int getTableIndex() {
			return tableIndex;
		}

		List<Instruction> getOffsetInstructions() {
			return offsetInstructions;
		}

		int[] getElements() {
			return elements;
		}
	}

	static class MemoryType {
		private final int minimumPageLength;
		private final int maximumPageLength;

		private final List<Data> data = new LinkedList<>();

		MemoryType(int minimumPageLength, int maximumPageLength) {
			this.minimumPageLength = minimumPageLength;
			this.maximumPageLength = maximumPageLength;
		}

		int getMinimumPageLength() {
			return minimumPageLength;
		}

		int getMaximumPageLength() {
			return maximumPageLength;
		}

		void addData(List<Instruction> offsetInstructions, byte[] data) {
			this.data.add(new Data(offsetInstructions, data));
		}

		List<Data> getData() {
			return data;
		}

		static class Data {
			private final List<Instruction> offsetInstructions;
			private final byte[] data;

			Data(List<Instruction> offsetInstructions, byte[] data) {
				this.offsetInstructions = offsetInstructions;
				this.data = data;
			}

			List<Instruction> getOffsetInstructions() {
				return offsetInstructions;
			}

			byte[] getData() {
				return data;
			}
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

	final static class CallIndirect implements Instruction {
		private final FunctionType functionType;
		private final int tableIndex;

		CallIndirect(Module module, int typeIndex, int tableIndex) {
			functionType = module.getFunctionType(typeIndex);
			this.tableIndex = tableIndex;
		}

		@Override
		public void execute(Frame frame) {
			int functionIndex = frame.pop().getI32();
			Function function = frame.getInstance().getTable(tableIndex).getFunction(functionIndex);
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

	static abstract class MemoryAccess extends PushValue {
		private final int align;
		private final int offset;

		MemoryAccess(int align, int offset) {
			this.align = align;
			this.offset = offset;
		}

		@Override
		final Value getValue(Frame frame) {
			int address = offset + frame.pop().getI32();
			Memory memory = frame.getInstance().getMemory();
			return readMemory(memory, address);
		}

		abstract Value readMemory(Memory memory, int address);
	}

	final static class I32Load8S extends MemoryAccess {
		I32Load8S(int align, int offset) {
			super(align, offset);
		}

		@Override
		Value readMemory(Memory memory, int address) {
			return new Value(memory.readInt8(address));
		}
	}

	final static class I32Load8U extends MemoryAccess {
		I32Load8U(int align, int offset) {
			super(align, offset);
		}

		@Override
		Value readMemory(Memory memory, int address) {
			return new Value(memory.readUint8AsInt(address));
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
		public final void execute(Frame frame) {
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

	final static class I32Eqz extends I32Converter {
		@Override
		int convert(int value) {
			return value == 0? 1: 0;
		}
	}

	final static class I32Eq extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first == second? 1: 0;
		}
	}

	final static class I32Ne extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first != second? 1: 0;
		}
	}

	final static class I32LtS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first < second? 1: 0;
		}
	}

	final static class I32LtU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return compareUnsigned(first, second) < 0? 1: 0;
		}
	}

	final static class I32GtS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first > second? 1: 0;
		}
	}

	final static class I32GtU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return 0 < compareUnsigned(first, second)? 1: 0;
		}
	}

	final static class I32LeS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first <= second? 1: 0;
		}
	}

	final static class I32LeU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return compareUnsigned(first, second) <= 0? 1: 0;
		}
	}

	final static class I32GeS extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return first >= second? 1: 0;
		}
	}

	final static class I32GeU extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return 0 <= compareUnsigned(first, second)? 1: 0;
		}
	}

	final static class F32Eq extends F32TwoOperandsCmpOperator {
		@Override
		boolean calculate(float first, float second) {
			return first == second;
		}
	}

	final static class F32Ne extends F32TwoOperandsCmpOperator {
		@Override
		boolean calculate(float first, float second) {
			return first != second;
		}
	}

	final static class F32Lt extends F32TwoOperandsCmpOperator {
		@Override
		boolean calculate(float first, float second) {
			return first < second;
		}
	}

	final static class F32Gt extends F32TwoOperandsCmpOperator {
		@Override
		boolean calculate(float first, float second) {
			return first > second;
		}
	}

	final static class F32Le extends F32TwoOperandsCmpOperator {
		@Override
		boolean calculate(float first, float second) {
			return first <= second;
		}
	}

	final static class F32Ge extends F32TwoOperandsCmpOperator {
		@Override
		boolean calculate(float first, float second) {
			return first >= second;
		}
	}

	final static class F64Eq extends F64TwoOperandsCmpOperator {
		@Override
		boolean calculate(double first, double second) {
			return first == second;
		}
	}

	final static class F64Ne extends F64TwoOperandsCmpOperator {
		@Override
		boolean calculate(double first, double second) {
			return first != second;
		}
	}

	final static class F64Lt extends F64TwoOperandsCmpOperator {
		@Override
		boolean calculate(double first, double second) {
			return first < second;
		}
	}

	final static class F64Gt extends F64TwoOperandsCmpOperator {
		@Override
		boolean calculate(double first, double second) {
			return first > second;
		}
	}

	final static class F64Le extends F64TwoOperandsCmpOperator {
		@Override
		boolean calculate(double first, double second) {
			return first <= second;
		}
	}

	final static class F64Ge extends F64TwoOperandsCmpOperator {
		@Override
		boolean calculate(double first, double second) {
			return first >= second;
		}
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

	final static class I32PopCnt extends I32Converter {
		@Override
		int convert(int value) {
			return bitCount(value);
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
			return Integer.divideUnsigned(first, second);
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
			return Integer.remainderUnsigned(first, second);
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

	final static class I32Rotl extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return Integer.rotateLeft(first, second);
		}
	}

	final static class I32Rotr extends I32TwoOperandsOperator {
		@Override
		int calculate(int first, int second) {
			return Integer.rotateRight(first, second);
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

	final static class I64Sub extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first - second;
		}
	}

	final static class I64Mul extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first * second;
		}
	}

	final static class I64DivS extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			try {
				if (first == Long.MIN_VALUE && second == -1) {
					throw new TrapException("integer overflow");
				}
				return first / second;
			} catch (ArithmeticException e) {
				throw new TrapException("integer divide by zero", e);
			}
		}
	}

	final static class I64DivU extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			try {
				return divideUnsigned(first, second);
			} catch (ArithmeticException e) {
				throw new TrapException("integer divide by zero", e);
			}
		}
	}

	final static class I64RemS extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			try {
				return first % second;
			} catch (ArithmeticException e) {
				throw new TrapException("integer divide by zero", e);
			}
		}
	}

	final static class I64RemU extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			try {
				return remainderUnsigned(first, second);
			} catch (ArithmeticException e) {
				throw new TrapException("integer divide by zero", e);
			}
		}
	}

	final static class I64And extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first & second;
		}
	}

	final static class I64Or extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first | second;
		}
	}

	final static class I64Xor extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first ^ second;
		}
	}

	final static class I64Shl extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first << second;
		}
	}

	final static class I64ShrS extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first >> second;
		}
	}

	final static class I64ShrU extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return first >>> second;
		}
	}

	final static class I64Rotl extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return rotateLeft(first, (int) second);
		}
	}

	final static class I64Rotr extends I64TwoOperandsOperator {
		@Override
		long calculate(long first, long second) {
			return rotateRight(first, (int) second);
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

	private static abstract class F32TwoOperandsCmpOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			float second = frame.pop().getF32();
			float first = frame.pop().getF32();
			frame.pushI32(calculate(first, second)? 1: 0);
		}

		abstract boolean calculate(float first, float second);
	}

	private static abstract class F32OneOperandsOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			frame.pushF32(calculate(frame.pop().getF32()));
		}

		abstract float calculate(float value);
	}

	final static class F32Abs extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			return abs(value);
		}
	}

	final static class F32Neg extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			return -value;
		}
	}

	final static class F32Ceil extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			return (float) ceil(value);
		}
	}

	final static class F32Floor extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			return (float) floor(value);
		}
	}

	final static class F32Trunc extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			if (Float.isNaN(value) || Float.isInfinite(value)) {
				return value;
			}
			if (0 < value) {
				return (float) floor(value);
			} else {
				return (float) ceil(value);
			}
		}
	}

	final static class F32Nearest extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			return (float) rint(value);
		}
	}

	final static class F32Sqrt extends F32OneOperandsOperator {
		@Override
		float calculate(float value) {
			return (float) sqrt(value);
		}
	}

	final static class F32Add extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return first + second;
		}
	}

	final static class F32Sub extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return first - second;
		}
	}

	final static class F32Mul extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return first * second;
		}
	}

	final static class F32Div extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return first / second;
		}
	}

	final static class F32Min extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return Math.min(first, second);
		}
	}

	final static class F32Max extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return Math.max(first, second);
		}
	}

	final static class F32Copysign extends F32TwoOperandsOperator {
		@Override
		float calculate(float first, float second) {
			return copySign(first, second);
		}
	}

	private static abstract class F64OneOperandsOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			frame.pushF64(calculate(frame.pop().getF64()));
		}

		abstract double calculate(double value);
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

	private static abstract class F64TwoOperandsCmpOperator implements Instruction {
		@Override
		public void execute(Frame frame) {
			double second = frame.pop().getF64();
			double first = frame.pop().getF64();
			frame.pushI32(calculate(first, second)? 1: 0);
		}

		abstract boolean calculate(double first, double second);
	}

	final static class F64Abs extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			return abs(value);
		}
	}

	final static class F64Neg extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			return -value;
		}
	}

	final static class F64Ceil extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			return ceil(value);
		}
	}

	final static class F64Floor extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			return floor(value);
		}
	}

	final static class F64Trunc extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			if (isNaN(value) || isInfinite(value)) {
				return value;
			}
			if (0 < value) {
				return floor(value);
			} else {
				return ceil(value);
			}
		}
	}

	final static class F64Nearest extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			return rint(value);
		}
	}

	final static class F64Sqrt extends F64OneOperandsOperator {
		@Override
		double calculate(double value) {
			return sqrt(value);
		}
	}

	final static class F64Add extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return first + second;
		}
	}

	final static class F64Sub extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return first - second;
		}
	}

	final static class F64Mul extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return first * second;
		}
	}

	final static class F64Div extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return first / second;
		}
	}

	final static class F64Min extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return Math.min(first, second);
		}
	}

	final static class F64Max extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return Math.max(first, second);
		}
	}

	final static class F64Copysign extends F64TwoOperandsOperator {
		@Override
		double calculate(double first, double second) {
			return copySign(first, second);
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
