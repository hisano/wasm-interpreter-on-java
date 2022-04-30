package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class BrIfTest {
	@Test
	void type_i32() throws IOException {
		Interpreter interpreter = getInterpreter();

		interpreter.getExportedFunction("type-i32").invoke();
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"0,2",
		"1,3",
	})
	void as_block_first(long value, long expectedValue) throws IOException {
		invoke("as-block-first", value, expectedValue);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"0,2",
		"1,3",
	})
	void as_block_mid(long value, long expectedValue) throws IOException {
		invoke("as-block-mid", value, expectedValue);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"0,2",
		"1,4",
	})
	void as_loop_mid(long value, long expectedValue) throws IOException {
		invoke("as-loop-mid", value, expectedValue);
	}

	@Test
	void as_br_value() throws IOException {
		invoke("as-br-value", 1);
	}

	private static void invoke(String functionName, long expectedValue) throws IOException {
		Interpreter interpreter = getInterpreter();

		int resultValue = (Integer) interpreter.getExportedFunction(functionName).invoke();

		assertEquals((int) expectedValue, resultValue);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"0,2",
		"1,1",
	})
	void as_br_if_value_cond(long value, long expectedValue) throws IOException {
		invoke("as-br_if-value-cond", value, expectedValue);
	}

	private static void invoke(String functionName, long value, long expectedValue) throws IOException {
		Interpreter interpreter = getInterpreter();

		int resultValue = (Integer) interpreter.getExportedFunction(functionName).invoke((int) value);

		assertEquals((int) expectedValue, resultValue);
	}

	@Test
	void as_br_table_value() throws IOException {
		invoke("as-br_table-value", 1);
	}

	@Test
	void as_br_table_value_index() throws IOException {
		invoke("as-br_table-value-index", 1);
	}

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
		"0,2",
		"1,1",
	})
	void as_if_cond(long value, long expectedValue) throws IOException {
		invoke("as-if-cond", value, expectedValue);
	}

	private static Interpreter getInterpreter() throws IOException {
		return createInterpreter("spec/br_if/br_if.0.wasm");
	}
}
