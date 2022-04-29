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
			int instruction = readUnsignedByte();
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
					int right = popI32();
					int left = popI32();
					pushI32(left + right);
					break;
				}
				case 0x6b: {
					// i32.sub
					int right = popI32();
					int left = popI32();
					pushI32(left - right);
					break;
				}
				case 0x6c: {
					// i32.mul
					int right = popI32();
					int left = popI32();
					pushI32(left * right);
					break;
				}
				case 0x6d: {
					// i32.div_s
					int right = popI32();
					int left = popI32();
					pushI32(left / right);
					break;
				}
				case 0x73: {
					// i32.xor
					int right = popI32();
					int left = popI32();
					pushI32(left ^ right);
					break;
				}

				case 0xC0: {
					// i32.extend8_s
					pushI32((byte)popI32());
					break;
				}
				case 0xC1: {
					// i32.extend16_s
					pushI32((short)popI32());
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
