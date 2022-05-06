package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class F64BitwiseTest {
	@ParameterizedTest(name = "copysign({0},{1}) = {2} (f64.copysign)")
	@CsvSource({
		"1,-2,-1",
	})
	void copysign(double first, double second, double expectedValue) throws IOException {
		invoke("copysign", first, second, expectedValue);
	}

	@ParameterizedTest(name = "abs({0}) = {1} (f64.abs)")
	@CsvSource({
		"-1,1",
	})
	void abs(double value, double expectedValue) throws IOException {
		invoke("abs", value,expectedValue);
	}

	@ParameterizedTest(name = "neg({0}) = {1} (f64.neg)")
	@CsvSource({
		"-1,1",
	})
	void neg(double value, double expectedValue) throws IOException {
		invoke("neg", value,expectedValue);
	}

	private static void invoke(String operatorName, double value, double expectedValue) throws IOException {
		double resultValue = createInterpreter("spec/f64_bitwise/f64_bitwise.0.wasm").invoke(operatorName, value);
		assertEquals(expectedValue, resultValue);
	}

	private static void invoke(String operatorName, double first, double second, double expectedValue) throws IOException {
		double resultValue = createInterpreter("spec/f64_bitwise/f64_bitwise.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
