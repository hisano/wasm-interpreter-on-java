package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {
	@ParameterizedTest(name = "{0}({1}) = {2}")
	@CsvSource({
		"8u_good1,0,97",
	})
	void readMemory(String functionName, int address, int expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/address/address.0.wasm").invoke(functionName, address);
		assertEquals(expectedValue, resultValue);
	}
}
