package jp.hisano.wasm.interpreter;

import java.io.IOException;

import static com.google.common.io.Resources.*;
import static org.junit.jupiter.api.Assertions.*;

final class TestUtils {
	static void branchIf(String functionName, long value, long expectedValue) throws IOException {
		Interpreter interpreter = createInterpreter("spec/br_if/br_if.0.wasm");

		int resultValue = (Integer) interpreter.getExportedFunction(functionName).invoke((int)value);

		assertEquals((int)expectedValue, resultValue);
	}

	static void calculate(String operatorName, long value, long expectedValue) throws IOException {
		Interpreter interpreter = createInterpreter("spec/i32/i32.0.wasm");

		int resultValue = (Integer) interpreter.getExportedFunction(operatorName).invoke((int)value);

		assertEquals((int)expectedValue, resultValue);
	}

	static void calculate(String operatorName, long first, long second, long expectedValue) throws IOException {
		Interpreter interpreter = createInterpreter("spec/i32/i32.0.wasm");

		int resultValue = (Integer) interpreter.getExportedFunction(operatorName).invoke((int)first, (int)second);

		assertEquals((int)expectedValue, resultValue);
	}

	static Interpreter createInterpreter(String wasmFilePath) throws IOException {
		byte[] wasmBinary = toByteArray(getResource(TestUtils.class, wasmFilePath));
		return new Interpreter(wasmBinary);
	}

	private TestUtils() {
	}
}
