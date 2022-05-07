package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("nop.wast")
class NopTest {
	@DisplayName("as-func-first")
	@Test
	void as_func_first() throws IOException {
		invoke("as-func-first", 1);
	}

	private static void invoke(String functionName, int expectedResult) throws IOException {
		assertEquals(expectedResult, (int)invokeFunction(functionName));
	}

	private static <T> T invokeFunction(String functionName, Object... parameters) throws IOException {
		return createInterpreter("spec/nop/nop.0.wasm").invoke(functionName, parameters);
	}
}
