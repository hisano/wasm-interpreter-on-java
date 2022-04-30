package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

// https://www.w3.org/TR/wasm-core-2/
class I32Test {
	@ParameterizedTest(name = "{0} + {1} = {2} (i32.add)")
	@CsvSource({
		"1,1,2",
		"1,0,1",
		"4294967295,4294967295,4294967294",
		"4294967295,1,0",
		"2147483647,1,2147483648",
		"2147483648,4294967295,2147483647",
		"2147483648,2147483648,0",
		"1073741823,1,1073741824",
	})
	void add(long first, long second, long expectedValue) throws IOException {
		invoke("add", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} - {1} = {2} (i32.sub)")
	@CsvSource({
		"1,1,0",
		"1,0,1",
	})
	void sub(long first, long second, long expectedValue) throws IOException {
		invoke("sub", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} * {1} = {2} (i32.mul)")
	@CsvSource({
		"1,1,1",
		"1,0,0",
		"4294967295,4294967295,1",
		"268435456,4096,0",
		"2147483648,0,0",
		"2147483648,4294967295,2147483648",
		"2147483647,4294967295,2147483649",
		"19088743,1985229328,898528368",
		"2147483647,2147483647,1",
	})
	void mul(long first, long second, long expectedValue) throws IOException {
		invoke("mul", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} / {1} = {2} (i32.div_s)")
	@CsvSource({
		"1,1,1",
		"0,1,0",
		"2147483648,2,3221225472",
	})
	void div_s(long first, long second, long expectedValue) throws IOException {
		invoke("div_s", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} ^ {1} = {2} (i32.xor)")
	@CsvSource({
		"1,0,1",
		"0,1,1",
		"1,1,0",
		"0,0,0",
		"2147483647,2147483648,4294967295",
		"2147483648,0,2147483648",
		"4294967295,2147483648,2147483647",
		"4294967295,2147483647,2147483648",
		"4042326015,4294963440,252645135",
		"4294967295,4294967295,0",
	})
	void xor(long first, long second, long expectedValue) throws IOException {
		invoke("xor", first, second, expectedValue);
	}

	// https://github.com/WebAssembly/sign-extension-ops
	@Nested
	@DisplayName("Sign Extension Operators")
	class SignExtensionOperatorsTest {
		@ParameterizedTest(name = "{0} -> {1} (i32.extend8_s)")
		@CsvSource({
				"0,0",
				"127,127",
				"128,4294967168",
				"255,4294967295",
				"19088640,0",
				"4275878528,4294967168",
				"4294967295,4294967295",
		})
		void extend8_s(long value, long expectedValue) throws IOException {
			invoke("extend8_s", value, expectedValue);
		}

		@ParameterizedTest(name = "{0} -> {1} (i32.extend16_s)")
		@CsvSource({
				"0,0",
				"32767,32767",
				"32768,4294934528",
				"65535,4294967295",
				"19070976,0",
				"4275863552,4294934528",
				"4294967295,4294967295",
		})
		void extend16_s(long value, long expectedValue) throws IOException {
			invoke("extend16_s", value, expectedValue);
		}
	}

	private static void invoke(String operatorName, long value, long expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/i32/i32.0.wasm").invoke(operatorName, (int) value);
		assertEquals((int)expectedValue, resultValue);
	}

	private static void invoke(String operatorName, long first, long second, long expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/i32/i32.0.wasm").invoke(operatorName, (int) first, (int) second);
		assertEquals((int)expectedValue, resultValue);
	}
}
