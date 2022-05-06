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
			"-0x0p+0,-0x0p+0,0x0p+0",
	})
	void sub(float first, float second, float expectedValue) throws IOException {
		invoke("sub", first, second, expectedValue);
	}

	private static void invoke(String operatorName, float first, float second, float expectedValue) throws IOException {
		float resultValue = createInterpreter("spec/f32/f32.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
