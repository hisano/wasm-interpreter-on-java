package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.google.common.io.Resources.*;
import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
	@Test
	void addTwo() throws IOException {
		Interpreter interpreter = createInterpreter("basic/addTwo.wasm");

		int resultValue = (Integer) interpreter.getExportedFunction("addTwo").invoke(1, 1);

		assertEquals(2, resultValue);
	}

	private Interpreter createInterpreter(String wasmFilePath) throws IOException {
		byte[] wasmBinary = toByteArray(getResource(InterpreterTest.class, wasmFilePath));
		return new Interpreter(wasmBinary);
	}

	@ParameterizedTest(name = "{0} + {1} = {2} (i32)")
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
		calculate("add", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} - {1} = {2} (i32)")
	@CsvSource({
		"1,1,0",
		"1,0,1",
	})
	void sub(long first, long second, long expectedValue) throws IOException {
		calculate("sub", first, second, expectedValue);
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
		calculate("mul", first, second, expectedValue);
	}

	@ParameterizedTest(name = "{0} / {1} = {2} (i32.div_s)")
	@CsvSource({
		"1,1,1",
		"0,1,0",
		"2147483648,2,3221225472",
	})
	void div_s(long first, long second, long expectedValue) throws IOException {
		calculate("div_s", first, second, expectedValue);
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
		calculate("xor", first, second, expectedValue);
	}

	private void calculate(String operatorName, long first, long second, long expectedValue) throws IOException {
		Interpreter interpreter = createInterpreter("spec/i32.0.wasm");

		int resultValue = (Integer) interpreter.getExportedFunction(operatorName).invoke((int)first, (int)second);

		assertEquals((int)expectedValue, resultValue);
	}
}
