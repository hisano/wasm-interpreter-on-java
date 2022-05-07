package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("local_get")
class LocalGetTest {
	@DisplayName("type-local-i32")
	@Test
	void type_local_i32() throws IOException {
		assertEquals(0, (int) invoke("type-local-i32"));
	}

	private <T> T invoke(String functionName, Object... parameters) throws IOException {
		return createInterpreter("spec/local_get/local_get.0.wasm").invoke(functionName, parameters);
	}
}
