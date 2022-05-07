package jp.hisano.wasm.interpreter;

public final class Memory {
	private final byte[] data;
	private final int maximumPageLength;

	public Memory(int initialPageLength, int maximumPageLength) {
		data = new byte[initialPageLength * 64 * 1024];
		this.maximumPageLength = maximumPageLength;
	}

	void setData(int offset, byte[] data) {
		System.arraycopy(data, 0, this.data,offset,data.length);
	}

	int readInt32(int address) {
		return readInt8(address) | (readUint8AsInt(address + 1) << 8) | (readUint8AsInt(address + 2) << 16) | (readUint8AsInt(address + 3) << 24);
	}

	int readInt16AsInt(int address) {
		return (short) readUint16AsInt(address);
	}

	int readUint16AsInt(int address) {
		return readUint8AsInt(address) | (readUint8AsInt(address + 1) << 8);
	}

	int readUint8AsInt(int address) {
		return readInt8(address) & 0xff;
	}

	byte readInt8(int address) {
		return data[address];
	}
}
