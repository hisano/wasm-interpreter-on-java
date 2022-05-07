package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("local_get")
class LocalGetTest {
	@DisplayName("type-local-i32")
	@Test
	void type_local_i32() throws IOException {
		assertEquals(0, (int) invoke("type-local-i32"));
	}

	@DisplayName("type-local-i64")
	@Test
	void type_local_i64() throws IOException {
		assertEquals(0, (long) invoke("type-local-i64"));
	}

	@DisplayName("type-local-f32")
	@Test
	void type_local_f32() throws IOException {
		assertEquals(0, (float) invoke("type-local-f32"));
	}

	@DisplayName("type-local-f64")
	@Test
	void type_local_f64() throws IOException {
		assertEquals(0, (double) invoke("type-local-f64"));
	}

	@DisplayName("type-param-i32")
	@Test
	void type_param_i32() throws IOException {
		assertEquals(2, (int) invoke("type-param-i32", 2));
	}

	@DisplayName("type-param-i64")
	@Test
	void type_param_i64() throws IOException {
		assertEquals(3, (long) invoke("type-param-i64", 3L));
	}

	@DisplayName("type-param-f32")
	@Test
	void type_param_f32() throws IOException {
		assertEquals(4.4f, (float) invoke("type-param-f32", 4.4f));
	}

	@DisplayName("type-param-f64")
	@Test
	void type_param_f64() throws IOException {
		assertEquals(5.5, (double) invoke("type-param-f64", 5.5));
	}

	@DisplayName("as-block-value")
	@Test
	void as_block_value() throws IOException {
		assertEquals(6, (int) invoke("as-block-value", 6));
	}

	@DisplayName("as-loop-value")
	@Test
	void as_loop_value() throws IOException {
		assertEquals(7, (int) invoke("as-loop-value", 7));
	}

	@DisplayName("as-br-value")
	@Test
	void as_br_value() throws IOException {
		assertEquals(8, (int) invoke("as-br-value", 8));
	}

	@DisplayName("as-br_if-value")
	@Test
	void as_br_if_value() throws IOException {
		assertEquals(9, (int) invoke("as-br_if-value", 9));
	}

	@DisplayName("as-br_if-value-cond")
	@Test
	void as_br_if_value_cond() throws IOException {
		assertEquals(10, (int) invoke("as-br_if-value-cond", 10));
	}

	@DisplayName("as-br_table-value")
	@Test
	void as_br_table_value() throws IOException {
		assertEquals(2, (int) invoke("as-br_table-value", 1));
	}

	@DisplayName("as-return-value")
	@Test
	void as_return_value() throws IOException {
		assertEquals(0, (int) invoke("as-return-value", 0));
	}

	@DisplayName("as-if-then")
	@Test
	void as_if_then() throws IOException {
		assertEquals(1, (int) invoke("as-if-then", 1));
	}

	@DisplayName("as-if-else")
	@Test
	void as_if_else() throws IOException {
		assertEquals(0, (int) invoke("as-if-else", 0));
	}

	@DisplayName("type-mixed")
	@Test
	void type_mixed() throws IOException {
		invoke("type-mixed", 1, 2.2f, 3.3, 4, 5);
	}

	@DisplayName("read")
	@Test
	void read() throws IOException {
		assertEquals(34.8, (double) invoke("read", 1L, 2f, 3.3, 4, 5));
	}

	private <T> T invoke(String functionName, Object... parameters) throws IOException {
		return createInterpreter("spec/local_get/local_get.0.wasm").invoke(functionName, parameters);
	}
}
