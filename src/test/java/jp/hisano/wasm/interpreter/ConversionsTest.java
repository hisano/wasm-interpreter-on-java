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

	@DisplayName("i32.wrap_i64")
	@ParameterizedTest(name = "i32.wrap_i64({0}) = {1}")
	@CsvSource({
		"-1,-1",
		"-100000,-100000",
		"0x80000000,0x80000000",
		"0xffffffff7fffffff,0x7fffffff",
		"0xffffffff00000000,0x00000000",
		"0xfffffffeffffffff,0xffffffff",
		"0xffffffff00000001,0x00000001",
		"0,0",
		"1311768467463790320,0x9abcdef0",
		"0x00000000ffffffff,0xffffffff",
		"0x0000000100000000,0x00000000",
		"0x0000000100000001,0x00000001",
	})
	void i32_wrap_i64(@WastValue long parameter, @WastValue int expectedResult) throws IOException {
		assertEquals(expectedResult, (int) invoke("i32.wrap_i64", parameter));
	}

	@DisplayName("i32.trunc_f32_s")
	@ParameterizedTest(name = "i32.trunc_f32_s({0}) = {1}")
	@CsvSource({
		"0.0,0",
		"-0.0,0",
		"0x1p-149,0",
		"-0x1p-149,0",
		"1.0,1",
		"0x1.19999ap+0,1",
		"1.5,1",
		"-1.0,-1",
		"-0x1.19999ap+0,-1",
		"-1.5,-1",
		"-1.9,-1",
		"-2.0,-2",
		"2147483520.0,2147483520",
		"-2147483648.0,-2147483648",
	})
	void i32_trunc_f32_s(@WastValue float parameter, @WastValue int expectedResult) throws IOException {
		assertEquals(expectedResult, (int) invoke("i32.trunc_f32_s", parameter));
	}

	private <T> T invoke(String functionName, Object... parameters) throws IOException {
		return createInterpreter("spec/conversions/conversions.0.wasm").invoke(functionName, parameters);
	}
}
