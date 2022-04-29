package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
	@Test
	void addTwo() throws IOException {
		Interpreter interpreter = createInterpreter("basic/addTwo.wasm");

		int resultValue = (Integer) interpreter.getExportedFunction("addTwo").invoke(1, 1);

		assertEquals(2, resultValue);
	}
}
