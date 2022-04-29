package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;

// https://github.com/WebAssembly/sign-extension-ops
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
		calculate("extend8_s", value, expectedValue);
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
		calculate("extend16_s", value, expectedValue);
	}
}
