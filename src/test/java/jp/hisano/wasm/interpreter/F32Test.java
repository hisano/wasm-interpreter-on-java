package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class F32Test {
	@ParameterizedTest(name = "{0} + {1} = {2} (f32.add)")
	@CsvSource({
		"1,1,2",
	})
	void add(float first, float second, float expectedValue) throws IOException {
		invoke("add", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} - {1} = {2} (f32.sub)")
	@CsvSource({
		"1,1,0",
	})
	void sub(float first, float second, float expectedValue) throws IOException {
		invoke("sub", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} * {1} = {2} (f32.mul)")
	@CsvSource({
		"1,2,2",
	})
	void mul(float first, float second, float expectedValue) throws IOException {
		invoke("mul", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} / {1} = {2} (f32.div)")
	@CsvSource({
		"6,3,2",
	})
	void div(float first, float second, float expectedValue) throws IOException {
		invoke("div", first, second, expectedValue);
	}

	@ParameterizedTest(name = "min({0},{1}) = {2} (f32.min)")
	@CsvSource({
		"1,0,0",
	})
	void min(float first, float second, float expectedValue) throws IOException {
		invoke("min", first, second, expectedValue);
	}

	@ParameterizedTest(name = "max({0},{1}) = {2} (f32.max)")
	@CsvSource({
		"1,0,1",
	})
	void max(float first, float second, float expectedValue) throws IOException {
		invoke("max", first, second, expectedValue);
	}

	@ParameterizedTest(name = "sqrt({0}) = {1} (f32.sqrt)")
	@CsvSource({
		"4,2",
	})
	void sqrt(float value, float expectedValue) throws IOException {
		invoke("sqrt", value,expectedValue);
	}

	@ParameterizedTest(name = "floor({0}) = {1} (f32.floor)")
	@CsvSource({
		"1.1,1",
	})
	void floor(float value, float expectedValue) throws IOException {
		invoke("floor", value,expectedValue);
	}

	@ParameterizedTest(name = "ceil({0}) = {1} (f32.ceil)")
	@CsvSource({
		"1.1,2",
	})
	void ceil(float value, float expectedValue) throws IOException {
		invoke("ceil", value,expectedValue);
	}

	@ParameterizedTest(name = "trunc({0}) = {1} (f32.trunc)")
	@CsvSource({
		"1.1,1", 
		"-1.1,-1",
	})
	void trunc(float value, float expectedValue) throws IOException {
		invoke("trunc", value,expectedValue);
	}

	@ParameterizedTest(name = "nearest({0}) = {1} (f32.nearest)")
	@CsvSource({
		"5.2,5",
		"5.5,6",
		"4.5,4",
	})
	void nearest(float value, float expectedValue) throws IOException {
		invoke("nearest", value,expectedValue);
	}

	private static void invoke(String operatorName, float value, float expectedValue) throws IOException {
		float resultValue = createInterpreter("spec/f32/f32.0.wasm").invoke(operatorName, value);
		assertEquals(expectedValue, resultValue);
	}

	private static void invoke(String operatorName, float first, float second, float expectedValue) throws IOException {
		float resultValue = createInterpreter("spec/f32/f32.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
