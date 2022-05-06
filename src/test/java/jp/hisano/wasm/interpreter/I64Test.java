package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("i64.wast")
class I64Test {
	@DisplayName("i64.add")
	@ParameterizedTest(name = "i64.add({0}, {1}) = {2}")
	@CsvSource({
		"1,1,2",
		"1,0,1",
		"-1,-1,-2",
		"-1,1,0",
		"0x7fffffffffffffff,1,0x8000000000000000",
		"0x8000000000000000,-1,0x7fffffffffffffff",
		"0x8000000000000000,0x8000000000000000,0",
		"0x3fffffff,1,0x40000000",
	})
	void add(@WastValue long first, @WastValue long second, @WastValue long expectedValue) throws IOException {
		invoke("add", first, second, expectedValue);
	}

	@DisplayName("i64.sub")
	@ParameterizedTest(name = "i64.sub({0}, {1}) = {2}")
	@CsvSource({
		"1,1,0",
		"1,0,1",
		"-1,-1,0",
		"0x7fffffffffffffff,-1,0x8000000000000000",
		"0x8000000000000000,1,0x7fffffffffffffff",
		"0x8000000000000000,0x8000000000000000,0",
		"0x3fffffff,-1,0x40000000",
	})
	void sub(@WastValue long first, @WastValue long second, @WastValue long expectedValue) throws IOException {
		invoke("sub", first, second, expectedValue);
	}

	private static void invoke(String functionName, long firstParameter, long secondParameter, long expectedResult) throws IOException {
		assertEquals(expectedResult, createInterpreter("spec/i64/i64.0.wasm").<Long>invoke(functionName, firstParameter, secondParameter));
	}
}
