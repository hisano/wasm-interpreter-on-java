package jp.hisano.wasm.interpreter;

import java.io.IOException;

import static com.google.common.io.Resources.*;

final class TestUtils {
	static Interpreter createInterpreter(String wasmFilePath) throws IOException {
		byte[] wasmBinary = toByteArray(getResource(TestUtils.class, wasmFilePath));
		return new Interpreter(wasmBinary);
	}

	private TestUtils() {
	}
}
