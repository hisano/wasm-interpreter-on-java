package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class BrIfTest {
	@Test
	void type_i32() throws IOException {
		Interpreter interpreter = getInterpreter();

		interpreter.getExportedFunction("type-i32").invoke();
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"0,2",
		"1,3",
	})
	void as_block_first(long value, long expectedValue) throws IOException {
		Interpreter interpreter = getInterpreter();

		int resultValue = (Integer) interpreter.getExportedFunction("as-block-first").invoke((int) value);

		assertEquals((int) expectedValue, resultValue);
	}

	private static Interpreter getInterpreter() throws IOException {
		return createInterpreter("spec/br_if/br_if.0.wasm");
	}
}
