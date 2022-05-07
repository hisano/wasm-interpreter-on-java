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

	boolean canRead() {
		return readIndex < bytes.length;
	}

	void skipBytes(int count) {
		readIndex += count;
	}

	int getReadIndex() {
		return readIndex;
	}

	String readUtf8() {
		return new String(readInt8Array());
	}

	byte[] readInt8Array() {
		return readInt8Array(readVaruint32());
	}

	byte[] readInt8Array(int length) {
		byte[] result = new byte[length];
		System.arraycopy(bytes, readIndex, result, 0, length);
		readIndex += length;
		return result;
	}

	byte readUint1() {
		return readByte();
	}

	byte readVarsint7() {
		return readByte();
	}

	byte readVaruint7() {
		return readByte();
	}

	int readVarsint32() {
		return readSignedLeb128(this);
	}

	int readVaruint32() {
		return readUnsignedLeb128(this);
	}

	int[] readVaruint32Array() {
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

	float readFloat32() {
		return intBitsToFloat(readInt32());
	}

	double readFloat64() {
		return longBitsToDouble(readInt64());
	}

	int readUint32() {
		return readInt32();
	}

	int readInt32() {
		return readUint8AsInt() | (readUint8AsInt() << 8) | (readUint8AsInt() << 16) | (readUint8AsInt() << 24);
	}

	long readInt64() {
		return readUint8AsLong() | (readUint8AsLong() << 8) | (readUint8AsLong() << 16) | (readUint8AsLong() << 24) | (readUint8AsLong() << 32) | (readUint8AsLong() << 40) | (readUint8AsLong() << 48) | (readUint8AsLong() << 56);
	}

	private long readUint8AsLong() {
		return readByte() & 0xffL;
	}

	int readUint8AsInt() {
		return readByte() & 0xff;
	}

	byte readByte() {
		return bytes[readIndex++];
	}
}
