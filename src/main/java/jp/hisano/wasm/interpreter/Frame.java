package jp.hisano.wasm.interpreter;

import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;

final class Frame {
	private final int[] i32Locals;
	private final byte[] instructions;

	private int pc; // = Program Counter

	private final int[] i32Stack = new int[256];
	private int stackIndex = 0;

	Frame(Module.Function function) {
		i32Locals = new int[function.parameterTypes.length];
		instructions = function.instructions;
	}

	void setLocal(int index, int value) {
		i32Locals[index] = value;
	}

	void invoke() {
		while (true) {
			int instruction = readByte();
			switch (instruction) {
				case 0x0b: {
					// end
					return;
				}

				case 0x20: {
					// local.get
					pushI32(i32Locals[readUnsignedLeb128()]);
					break;
				}

				case 0x6a: {
					// i32.add
					pushI32(popI32() + popI32());
					break;
				}
				case 0x6b: {
					// i32.sub
					pushI32(popI32() - popI32());
					break;
				}

				default: {
					throw new UnsupportedOperationException("not implemented instruction (0x" + Integer.toHexString(instruction) + ")");
				}
			}
		}
	}

	private byte readByte() {
		return instructions[pc++];
	}

	private int readUnsignedByte() {
		return readByte() & 0xff;
	}

	private int readUnsignedLeb128() {
		int result = 0;

		int value;
		int index = 0;
		do {
			value = readUnsignedByte();
			result |= (value & 0x7f) << (index * 7);
			index++;
		} while (((value & 0x80) != 0) && index < 5);

		if ((value & 0x80) != 0) {
			throw new InterpreterException(ILLEGAL_BINARY);
		}

		return result;
	}

	int popI32() {
		return i32Stack[--stackIndex];
	}

	void pushI32(int value) {
		i32Stack[stackIndex++] = value;
	}
}
