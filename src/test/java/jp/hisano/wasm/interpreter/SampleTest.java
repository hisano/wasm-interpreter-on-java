package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class SampleTest {
	@Test
	void addTwo() throws IOException {
		int resultValue = createInterpreter("sample/add.wasm").invoke("add", 1, 1);
		assertEquals(2, resultValue);
	}
}
