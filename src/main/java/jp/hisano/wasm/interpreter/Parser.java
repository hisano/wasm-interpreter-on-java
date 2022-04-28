package jp.hisano.wasm.interpreter;

import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;
import jp.hisano.wasm.interpreter.Module.ValueType;

final class Parser {
	private static final int MAGIC = 0x6d736100; // = "\0asm"
	private static final int VERSION = 1;

	private final byte[] wasmBinary;

	private final Module module = new Module();

	private int readIndex;

	public Parser(byte[] wasmBinary) {
		this.wasmBinary = wasmBinary;
	}

	public Module parseModule() {
		checkInt(MAGIC);
		checkInt(VERSION);

		while (canRead()) {
			int section = readByte();
			int size = readUnsignedLeb128();
			switch (section) {
				case 0x01:
					readTypeSection();
					break;
			}
		}

		return module;
	}

	private void readTypeSection() {
		int length = readUnsignedLeb128();
		int index = 0;
		while (index < length) {
			switch (readByte()) {
				case 0x60:
					readFunctionType();
					break;
			}
			index++;
		}
	}

	private void readFunctionType() {
		module.addFunctionType(readResultTypes(), readResultTypes());
	}

	private ValueType[] readResultTypes() {
		int length = readUnsignedLeb128();
		ValueType[] result = new ValueType[length];

		int index = 0;
		while (index < length) {
			switch (readByte()) {
				case 0x7f:
					result[index] = ValueType.I32;
					break;
			}
			index++;
		}

		return result;
	}

	private boolean canRead() {
		return readIndex < wasmBinary.length; 
	}

	private void checkInt(int expectedValue) {
		if (readInt() != expectedValue) {
			throw new InterpreterException(ILLEGAL_BINARY);
		}
	}

	private int readInt() {
		return readUnsignedByte() | (readUnsignedByte() << 8) | (readUnsignedLeb128() << 16) | (readUnsignedByte() << 24);
	}

	private int readByte() {
		return wasmBinary[readIndex++];
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
}
