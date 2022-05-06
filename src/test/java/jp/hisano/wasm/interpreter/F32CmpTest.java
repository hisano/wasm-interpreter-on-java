package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class F32CmpTest {
	@ParameterizedTest(name = "{0} == {1} = {2} (f32.eq)")
	@CsvSource({
		"-0x0p+0,-0x0p+0,1",
	})
	void copysign(float first, float second, int expectedValue) throws IOException {
		invoke("eq", first, second, expectedValue);
	}

	private static void invoke(String operatorName, float first, float second, int expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/f32_cmp/f32_cmp.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
