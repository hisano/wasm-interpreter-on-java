package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class GlobalTest {
	@Test
	void get_a() throws IOException {
		Interpreter interpreter = getInterpreter();

		int resultValue = (Integer) interpreter.getExportedFunction("get-a").invoke();

		assertEquals(-2, resultValue);
	}

	private static Interpreter getInterpreter() throws IOException {
		return createInterpreter("spec/global/global.0.wasm");
	}
}
