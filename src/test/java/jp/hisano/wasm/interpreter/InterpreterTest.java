package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
	@Test
	void addTwo() throws IOException {
		int resultValue = createInterpreter("basic/addTwo.wasm").invoke("addTwo", 1, 1);
		assertEquals(2, resultValue);
	}
}
