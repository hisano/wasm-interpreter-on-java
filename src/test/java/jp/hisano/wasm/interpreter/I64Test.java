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
	void add(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("add", firstParameter, secondParameter, expectedResult);
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
	void sub(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("sub", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.mul")
	@ParameterizedTest(name = "i64.mul({0}, {1}) = {2}")
	@CsvSource({
		"1,1,1",
		"1,0,0",
		"-1,-1,1",
		"0x1000000000000000,4096,0",
		"0x8000000000000000,0,0",
		"0x8000000000000000,-1,0x8000000000000000",
		"0x7fffffffffffffff,-1,0x8000000000000001",
		"0x0123456789abcdef,0xfedcba9876543210,0x2236d88fe5618cf0",
		"0x7fffffffffffffff,0x7fffffffffffffff,1",
	})
	void mul(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("mul", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.div_s (trap)")
	@ParameterizedTest(name = "i64.div_s({0}, {1}) = \"{2}\"")
	@CsvSource({
		"1,0,integer divide by zero",
		"0,0,integer divide by zero",
		"0x8000000000000000,-1,integer overflow",
		"0x8000000000000000,0,integer divide by zero",
	})
	void div_s_trap(@WastValue long firstParameter, @WastValue long secondParameter, String expectedTrapMessage) throws IOException {
		invokeTrap("div_s", firstParameter, secondParameter, expectedTrapMessage);
	}

	@DisplayName("i64.div_s")
	@ParameterizedTest(name = "i64.div_s({0}, {1}) = {2}")
	@CsvSource({
		"1,1,1",
		"0,1,0",
		"0,-1,0",
		"-1,-1,1",
		"0x8000000000000000,2,0xc000000000000000",
		"0x8000000000000001,1000,0xffdf3b645a1cac09",
		"5,2,2",
		"-5,2,-2",
		"5,-2,-2",
		"-5,-2,2",
		"7,3,2",
		"-7,3,-2",
		"7,-3,-2",
		"-7,-3,2",
		"11,5,2",
		"17,7,2",
	})
	void div_s(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("div_s", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.div_u (trap)")
	@ParameterizedTest(name = "i64.div_u({0}, {1}) = \"{2}\"")
	@CsvSource({
		"1,0,integer divide by zero",
		"0,0,integer divide by zero",
	})
	void div_u_trap(@WastValue long firstParameter, @WastValue long secondParameter, String expectedTrapMessage) throws IOException {
		invokeTrap("div_u", firstParameter, secondParameter, expectedTrapMessage);
	}

	@DisplayName("i64.div_u")
	@ParameterizedTest(name = "i64.div_u({0}, {1}) = {2}")
	@CsvSource({
		"1,1,1",
		"0,1,0",
		"-1,-1,1",
		"0x8000000000000000,-1,0",
		"0x8000000000000000,2,0x4000000000000000",
		"0x8ff00ff00ff00ff0,0x100000001,0x8ff00fef",
		"0x8000000000000001,1000,0x20c49ba5e353f7",
		"5,2,2",
		"-5,2,0x7ffffffffffffffd",
		"5,-2,0",
		"-5,-2,0",
		"7,3,2",
		"11,5,2",
		"17,7,2",
	})
	void div_u(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("div_u", firstParameter, secondParameter, expectedResult);
	}

	private static void invokeTrap(String functionName, long firstParameter, long secondParameter, String expectedTrapMessage) throws IOException {
		TrapException trapException = assertThrows(TrapException.class, () -> {
			invoke(functionName, firstParameter, secondParameter);
		});
		assertEquals(expectedTrapMessage, trapException.getMessage());
	}

	private static void invoke(String functionName, long firstParameter, long secondParameter, long expectedResult) throws IOException {
		assertEquals(expectedResult, invoke(functionName, firstParameter, secondParameter));
	}

	private static long invoke(String functionName, long firstParameter, long secondParameter) throws IOException {
		return createInterpreter("spec/i64/i64.0.wasm").invoke(functionName, firstParameter, secondParameter);
	}
}
