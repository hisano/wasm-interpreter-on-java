package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class F32CmpTest {
	@ParameterizedTest(name = "({0} == {1}) = {2} (f32.eq)")
	@CsvSource({
		"-0x0p+0,-0x0p+0,1",
	})
	void copysign(float first, float second, int expectedValue) throws IOException {
		invoke("eq", first, second, expectedValue);
	}

	@ParameterizedTest(name = "({0} != {1}) = {2} (f32.ne)")
	@CsvSource({
		"-0x0p+0,-0x0p+0,0",
	})
	void ne(float first, float second, int expectedValue) throws IOException {
		invoke("ne", first, second, expectedValue);
	}

	@ParameterizedTest(name = "({0} < {1}) = {2} (f32.lt)")
	@CsvSource({
		"0x1p-149,-0x1p-126,0",
	})
	void lt(float first, float second, int expectedValue) throws IOException {
		invoke("lt", first, second, expectedValue);
	}

	@ParameterizedTest(name = "({0} <= {1}) = {2} (f32.le)")
	@CsvSource({
		"-0x1p-149,-0x1p-149,1",
		"-0x1p-149,0x1p-149,1",
		"0x1p-149,-0x1p-149,0",
	})
	void le(float first, float second, int expectedValue) throws IOException {
		invoke("le", first, second, expectedValue);
	}

	@ParameterizedTest(name = "({0} > {1}) = {2} (f32.gt)")
	@CsvSource({
		"-0x1p-149,-0x1p-149,0",
		"-0x1p-149,0x1p-149,0",
		"0x1p-149,-0x1p-149,1",
	})
	void gt(float first, float second, int expectedValue) throws IOException {
		invoke("gt", first, second, expectedValue);
	}

	private static void invoke(String operatorName, float first, float second, int expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/f32_cmp/f32_cmp.0.wasm").invoke(operatorName, first, second);
		assertEquals(expectedValue, resultValue);
	}
}
