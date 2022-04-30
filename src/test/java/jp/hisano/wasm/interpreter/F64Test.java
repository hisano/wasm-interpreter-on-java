package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class F64Test {
	@ParameterizedTest(name = "{0} + {1} = {2} (f64.add)")
	@CsvSource({
		"1,1,2",
	})
	void add(double first, double second, double expectedValue) throws IOException {
		invoke("add", first, second, expectedValue);
	}

	private static void invoke(String operatorName, double first, double second, double expectedValue) throws IOException {
		double resultValue = createInterpreter("spec/f64/f64.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
