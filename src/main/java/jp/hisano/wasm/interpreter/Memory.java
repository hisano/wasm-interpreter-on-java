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

	public long readInt64(int address) {
		return (readUint8AsInt(address) & 0xFFL) |
				((readUint8AsInt(address + 1) & 0xFFL) << 8) |
				((readUint8AsInt(address + 2) & 0xFFL) << 16) |
				((readUint8AsInt(address + 3) & 0xFFL) << 24) |
				((readUint8AsInt(address + 4) & 0xFFL) << 32) |
				((readUint8AsInt(address + 5) & 0xFFL) << 40) |
				((readUint8AsInt(address + 6) & 0xFFL) << 48) |
				((readUint8AsInt(address + 7) & 0xFFL) << 56);
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
