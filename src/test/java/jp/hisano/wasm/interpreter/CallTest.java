package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class CallTest {
	@Test
	void type_i32() throws IOException {
		int resultValue = getInterpreter().invoke("type-i32");
		assertEquals(306, resultValue);
	}

	private static Interpreter getInterpreter() throws IOException {
		return createInterpreter("spec/call/call.0.wasm");
	}
}
