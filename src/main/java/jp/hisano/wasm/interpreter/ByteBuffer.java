package jp.hisano.wasm.interpreter;

import static jp.hisano.wasm.interpreter.InterpreterException.Type.*;

final class ByteBuffer {
	private final byte[] bytes;
	private int readIndex;

	ByteBuffer(byte[] bytes) {
		this.bytes = bytes;
	}

	void skipBytes(int count) {
		readIndex += count;
	}

	int getPosition() {
		return readIndex;
	}

	String readUtf8() {
		return new String(readBytes());
	}

	byte[] readBytes() {
		return readBytes(readUnsignedLeb128());
	}

	byte[] readBytes(int length) {
		byte[] result = new byte[length];
		System.arraycopy(bytes, readIndex, result, 0, length);
		readIndex += length;
		return result;
	}

	int readUnsignedLeb128() {
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

	int readInt() {
		return readUnsignedByte() | (readUnsignedByte() << 8) | (readUnsignedLeb128() << 16) | (readUnsignedByte() << 24);
	}

	int readUnsignedByte() {
		return readByte() & 0xff;
	}

	int readByte() {
		return bytes[readIndex++];
	}

	boolean canRead() {
		return readIndex < bytes.length;
	}

	public int[] readVarUInt32Array() {
		int length = readUnsignedLeb128();
		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			result[i] = readUnsignedLeb128();
		}
		return result;
	}
}
