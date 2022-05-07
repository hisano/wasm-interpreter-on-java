package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("address.wast")
class AddressTest {
	@DisplayName("i32.load8_u")
	@ParameterizedTest(name = "{0}({1}) = {2}")
	@CsvSource({
		"8u_good1,0,97",
		"8u_good2,0,97",
		"8u_good3,0,98",
		"8u_good4,0,99",
		"8u_good5,0,122",
	})
	void load8_u(String functionName, int address, int expectedValue) throws IOException {
		invoke(functionName, address, expectedValue);
	}

	@DisplayName("i32.load8_s")
	@ParameterizedTest(name = "{0}({1}) = {2}")
	@CsvSource({
		"8s_good1,0,97",
		"8s_good2,0,97",
		"8s_good3,0,98",
		"8s_good4,0,99",
		"8s_good5,0,122",
	})
	void load8_s(String functionName, int address, int expectedValue) throws IOException {
		invoke(functionName, address, expectedValue);
	}

	private void invoke(String functionName, int address, int expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/address/address.0.wasm").invoke(functionName, address);
		assertEquals(expectedValue, resultValue);
	}
}
