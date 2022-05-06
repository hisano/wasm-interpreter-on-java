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

	@ParameterizedTest(name = "{0} - {1} = {2} (f64.sub)")
	@CsvSource({
		"1,1,0",
	})
	void sub(double first, double second, double expectedValue) throws IOException {
		invoke("sub", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} * {1} = {2} (f64.mul)")
	@CsvSource({
		"1,2,2",
	})
	void mul(double first, double second, double expectedValue) throws IOException {
		invoke("mul", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} / {1} = {2} (f64.div)")
	@CsvSource({
		"6,3,2",
	})
	void div(double first, double second, double expectedValue) throws IOException {
		invoke("div", first, second, expectedValue);
	}

	@ParameterizedTest(name = "min({0},{1}) = {2} (f64.min)")
	@CsvSource({
		"1,0,0",
	})
	void min(double first, double second, double expectedValue) throws IOException {
		invoke("min", first, second, expectedValue);
	}

	@ParameterizedTest(name = "max({0},{1}) = {2} (f64.max)")
	@CsvSource({
		"1,0,1",
	})
	void max(double first, double second, double expectedValue) throws IOException {
		invoke("max", first, second, expectedValue);
	}

	@ParameterizedTest(name = "sqrt({0}) = {1} (f64.sqrt)")
	@CsvSource({
		"4,2",
	})
	void sqrt(double value, double expectedValue) throws IOException {
		invoke("sqrt", value,expectedValue);
	}

	@ParameterizedTest(name = "floor({0}) = {1} (f64.floor)")
	@CsvSource({
		"1.1,1",
	})
	void floor(double value, double expectedValue) throws IOException {
		invoke("floor", value,expectedValue);
	}

	@ParameterizedTest(name = "ceil({0}) = {1} (f64.ceil)")
	@CsvSource({
		"1.1,2",
	})
	void ceil(double value, double expectedValue) throws IOException {
		invoke("ceil", value,expectedValue);
	}

	@ParameterizedTest(name = "trunc({0}) = {1} (f64.trunc)")
	@CsvSource({
		"1.1,1",
		"-1.1,-1",
	})
	void trunc(double value, double expectedValue) throws IOException {
		invoke("trunc", value,expectedValue);
	}

	@ParameterizedTest(name = "nearest({0}) = {1} (f64.nearest)")
	@CsvSource({
		"5.2,5",
		"5.5,6",
		"4.5,4",
	})
	void nearest(double value, double expectedValue) throws IOException {
		invoke("nearest", value,expectedValue);
	}

	private static void invoke(String operatorName, double value, double expectedValue) throws IOException {
		double resultValue = createInterpreter("spec/f64/f64.0.wasm").invoke(operatorName, value);
		assertEquals(expectedValue, resultValue);
	}

	private static void invoke(String operatorName, double first, double second, double expectedValue) throws IOException {
		double resultValue = createInterpreter("spec/f64/f64.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
