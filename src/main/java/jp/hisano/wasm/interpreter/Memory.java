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

	int readUint8AsInt(int address) {
		return data[address] & 0xff;
	}

	byte readInt8(int address) {
		return data[address];
	}
}
