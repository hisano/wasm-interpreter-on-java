package jp.hisano.wasm.interpreter;

import static java.lang.Double.*;
import static java.lang.Float.*;
import static jp.hisano.wasm.interpreter.Leb128.*;

final class ByteBuffer {
	private final byte[] bytes;
	private int readIndex;

	ByteBuffer(byte[] bytes) {
		this.bytes = bytes;
	}

	void skipBytes(int count) {
		readIndex += count;
	}

	int getReadIndex() {
		return readIndex;
	}

	String readUtf8() {
		return new String(readBytes());
	}

	byte[] readBytes() {
		return readBytes(readVaruint32());
	}

	byte[] readBytes(int length) {
		byte[] result = new byte[length];
		System.arraycopy(bytes, readIndex, result, 0, length);
		readIndex += length;
		return result;
	}

	byte readUint1() {
		return bytes[readIndex++];
	}

	byte readVarsint7() {
		return bytes[readIndex++];
	}

	byte readVaruint7() {
		return bytes[readIndex++];
	}

	int readVarsint32() {
		return readSignedLeb128(this);
	}

	int readVaruint32() {
		return readUnsignedLeb128(this);
	}

	float readFloat32() {
		return intBitsToFloat(readInt());
	}

	double readFloat64() {
		// TODO -4が正しくデコードできない
		return longBitsToDouble(readLong());
	}

	long readLong() {
		return (long)readUnsignedByte() | (readUnsignedByte() << 8) | (readUnsignedByte() << 16) | (readUnsignedByte() << 24) | (readUnsignedByte() << 32) | (readUnsignedByte() << 40) | (readUnsignedByte() << 48) | (readUnsignedByte() << 56);
	}

	int readUint32() {
		return readInt();
	}

	int readInt() {
		return readUnsignedByte() | (readUnsignedByte() << 8) | (readUnsignedByte() << 16) | (readUnsignedByte() << 24);
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

	int[] readVarUInt32Array() {
		int length = readVaruint32();
		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			result[i] = readVaruint32();
		}
		return result;
	}

	long readVarsint64() {
		return readSignedLongLeb128(this);
	}
}
