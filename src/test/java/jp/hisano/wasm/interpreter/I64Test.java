package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class I64Test {
	@ParameterizedTest(name = "{0} + {1} = {2} (i64.add)")
	@CsvSource({
		"1,1,2",
	})
	void add(long first, long second, long expectedValue) throws IOException {
		invoke("add", first, second, expectedValue);
	}

	private static void invoke(String operatorName, long first, long second, long expectedValue) throws IOException {
		long resultValue = createInterpreter("spec/i64/i64.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
