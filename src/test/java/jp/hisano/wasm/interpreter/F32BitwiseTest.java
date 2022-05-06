package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class F32BitwiseTest {
	@ParameterizedTest(name = "copysign({0},{1}) = {2} (f32.copysign)")
	@CsvSource({
		"1,-2,-1",
	})
	void copysign(float first, float second, float expectedValue) throws IOException {
		invoke("copysign", first, second, expectedValue);
	}

	@ParameterizedTest(name = "abs({0}) = {1} (f32.abs)")
	@CsvSource({
			"-1,1",
	})
	void abs(float value, float expectedValue) throws IOException {
		invoke("abs", value,expectedValue);
	}

	private static void invoke(String operatorName, float value, float expectedValue) throws IOException {
		float resultValue = createInterpreter("spec/f32_bitwise/f32_bitwise.0.wasm").invoke(operatorName, value);
		assertEquals(expectedValue, resultValue);
	}

	private static void invoke(String operatorName, float first, float second, float expectedValue) throws IOException {
		float resultValue = createInterpreter("spec/f32_bitwise/f32_bitwise.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
