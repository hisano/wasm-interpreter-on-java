package jp.hisano.wasm.interpreter;

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
			switch (readByte()) {
				case 0x20: {
					// local.get
					i32Stack[stackIndex++] = i32Locals[readByte()];
					break;
				}
				case 0x6a: {
					// i32.add
					int baseIndex = stackIndex - 2;
					i32Stack[baseIndex] = i32Stack[baseIndex] + i32Stack[baseIndex + 1];
					stackIndex = baseIndex + 1;
					break;
				}
				case 0x0b: {
					// end
					return;
				}
			}
		}
	}

	private byte readByte() {
		return instructions[pc++];
	}

	int popI32() {
		return i32Stack[--stackIndex];
	}
}
