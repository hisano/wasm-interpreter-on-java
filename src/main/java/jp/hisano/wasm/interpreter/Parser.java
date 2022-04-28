package jp.hisano.wasm.interpreter;

import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;

final class Parser {
	private static final int MAGIC = 0x6d736100; // = "\0asm"
	private static final int VERSION = 1;

	private final byte[] _wasmBinary;

	private int _readIndex;

	public Parser(byte[] wasmBinary) {
		_wasmBinary = wasmBinary;
	}

	public Module parseModule() {
		checkInt(MAGIC);
		checkInt(VERSION);
		return null;
	}

	private void checkInt(int expectedValue) {
		if (readInt() != expectedValue) {
			throw new InterpreterException(ILLEGAL_BINARY);
		}
	}

	private int readInt() {
		return readByte() | (readByte() << 8) | (readByte() << 16) | (readByte() << 24);
	}

	private int readByte() {
		return _wasmBinary[_readIndex++];
	}
}
