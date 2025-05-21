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

        long readInt64(int address) {
                return readUint8AsLong(address)
                        | (readUint8AsLong(address + 1) << 8)
                        | (readUint8AsLong(address + 2) << 16)
                        | (readUint8AsLong(address + 3) << 24)
                        | (readUint8AsLong(address + 4) << 32)
                        | (readUint8AsLong(address + 5) << 40)
                        | (readUint8AsLong(address + 6) << 48)
                        | (readUint8AsLong(address + 7) << 56);
        }

        private long readUint8AsLong(int address) {
                return readInt8(address) & 0xffL;
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
