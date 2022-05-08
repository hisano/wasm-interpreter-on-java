package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("conversions.wast")
class ConversionsTest {
	@DisplayName("i64.extend_i32_s")
	@ParameterizedTest(name = "i64.extend_i32_s({0}) = {1}")
	@CsvSource({
		"0,0",
		"10000,10000",
		"-10000,-10000",
		"-1,-1",
		"0x7fffffff,0x000000007fffffff",
		"0x80000000,0xffffffff80000000",
	})
	void i64_extend_i32_s(@WastValue int parameter, @WastValue long expectedResult) throws IOException {
		assertEquals(expectedResult, (long) invoke("i64.extend_i32_s", parameter));
	}

	@DisplayName("i64.extend_i32_u")
	@ParameterizedTest(name = "i64.extend_i32_u({0}) = {1}")
	@CsvSource({
		"0,0",
		"10000,10000",
		"-10000,0x00000000ffffd8f0",
		"-1,0xffffffff",
		"0x7fffffff,0x000000007fffffff",
		"0x80000000,0x0000000080000000",
	})
	void i64_extend_i32_u(@WastValue int parameter, @WastValue long expectedResult) throws IOException {
		assertEquals(expectedResult, (long) invoke("i64.extend_i32_u", parameter));
	}

	private <T> T invoke(String functionName, Object... parameters) throws IOException {
		return createInterpreter("spec/conversions/conversions.0.wasm").invoke(functionName, parameters);
	}
}
