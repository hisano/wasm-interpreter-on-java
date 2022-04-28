package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static com.google.common.io.Resources.*;
import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
	@Test
	void addTwo() throws IOException {
		byte[] wasm = toByteArray(getResource(InterpreterTest.class, "basic/addTwo.wasm"));
		Interpreter interpreter = new Interpreter(wasm);

		int result = (Integer) interpreter.getExportedFunction("addTwo").invoke(1, 1);

		assertEquals(2, result);
	}
}
