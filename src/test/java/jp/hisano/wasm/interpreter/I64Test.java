package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("i64.wast")
class I64Test {
	@DisplayName("i64.add")
	@ParameterizedTest(name = "i64.add({0}, {1}) = {2}")
	@CsvSource({
		"1,1,2",
		"1,0,1",
		"-1,-1,-2",
		"-1,1,0",
		"0x7fffffffffffffff,1,0x8000000000000000",
		"0x8000000000000000,-1,0x7fffffffffffffff",
		"0x8000000000000000,0x8000000000000000,0",
		"0x3fffffff,1,0x40000000",
	})
	void add(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("add", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.sub")
	@ParameterizedTest(name = "i64.sub({0}, {1}) = {2}")
	@CsvSource({
		"1,1,0",
		"1,0,1",
		"-1,-1,0",
		"0x7fffffffffffffff,-1,0x8000000000000000",
		"0x8000000000000000,1,0x7fffffffffffffff",
		"0x8000000000000000,0x8000000000000000,0",
		"0x3fffffff,-1,0x40000000",
	})
	void sub(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("sub", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.mul")
	@ParameterizedTest(name = "i64.mul({0}, {1}) = {2}")
	@CsvSource({
		"1,1,1",
		"1,0,0",
		"-1,-1,1",
		"0x1000000000000000,4096,0",
		"0x8000000000000000,0,0",
		"0x8000000000000000,-1,0x8000000000000000",
		"0x7fffffffffffffff,-1,0x8000000000000001",
		"0x0123456789abcdef,0xfedcba9876543210,0x2236d88fe5618cf0",
		"0x7fffffffffffffff,0x7fffffffffffffff,1",
	})
	void mul(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("mul", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.div_s (trap)")
	@ParameterizedTest(name = "i64.div_s({0}, {1}) = \"{2}\"")
	@CsvSource({
		"1,0,integer divide by zero",
		"0,0,integer divide by zero",
		"0x8000000000000000,-1,integer overflow",
		"0x8000000000000000,0,integer divide by zero",
	})
	void div_s_trap(@WastValue long firstParameter, @WastValue long secondParameter, String expectedTrapMessage) throws IOException {
		invokeTrap("div_s", firstParameter, secondParameter, expectedTrapMessage);
	}

	@DisplayName("i64.div_s")
	@ParameterizedTest(name = "i64.div_s({0}, {1}) = {2}")
	@CsvSource({
		"1,1,1",
		"0,1,0",
		"0,-1,0",
		"-1,-1,1",
		"0x8000000000000000,2,0xc000000000000000",
		"0x8000000000000001,1000,0xffdf3b645a1cac09",
		"5,2,2",
		"-5,2,-2",
		"5,-2,-2",
		"-5,-2,2",
		"7,3,2",
		"-7,3,-2",
		"7,-3,-2",
		"-7,-3,2",
		"11,5,2",
		"17,7,2",
	})
	void div_s(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("div_s", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.div_u (trap)")
	@ParameterizedTest(name = "i64.div_u({0}, {1}) = \"{2}\"")
	@CsvSource({
		"1,0,integer divide by zero",
		"0,0,integer divide by zero",
	})
	void div_u_trap(@WastValue long firstParameter, @WastValue long secondParameter, String expectedTrapMessage) throws IOException {
		invokeTrap("div_u", firstParameter, secondParameter, expectedTrapMessage);
	}

	@DisplayName("i64.div_u")
	@ParameterizedTest(name = "i64.div_u({0}, {1}) = {2}")
	@CsvSource({
		"1,1,1",
		"0,1,0",
		"-1,-1,1",
		"0x8000000000000000,-1,0",
		"0x8000000000000000,2,0x4000000000000000",
		"0x8ff00ff00ff00ff0,0x100000001,0x8ff00fef",
		"0x8000000000000001,1000,0x20c49ba5e353f7",
		"5,2,2",
		"-5,2,0x7ffffffffffffffd",
		"5,-2,0",
		"-5,-2,0",
		"7,3,2",
		"11,5,2",
		"17,7,2",
	})
	void div_u(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("div_u", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.rem_s (trap)")
	@ParameterizedTest(name = "i64.rem_s({0}, {1}) = \"{2}\"")
	@CsvSource({
		"1,0,integer divide by zero",
		"0,0,integer divide by zero",
	})
	void rem_s_trap(@WastValue long firstParameter, @WastValue long secondParameter, String expectedTrapMessage) throws IOException {
		invokeTrap("rem_s", firstParameter, secondParameter, expectedTrapMessage);
	}

	@DisplayName("i64.rem_s")
	@ParameterizedTest(name = "i64.rem_s({0}, {1}) = {2}")
	@CsvSource({
		"0x7fffffffffffffff,-1,0",
		"1,1,0",
		"0,1,0",
		"0,-1,0",
		"-1,-1,0",
		"0x8000000000000000,-1,0",
		"0x8000000000000000,2,0",
		"0x8000000000000001,1000,-807",
		"5,2,1",
		"-5,2,-1",
		"5,-2,1",
		"-5,-2,-1",
		"7,3,1",
		"-7,3,-1",
		"7,-3,1",
		"-7,-3,-1",
		"11,5,1",
		"17,7,3",
	})
	void rem_s(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("rem_s", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.rem_u (trap)")
	@ParameterizedTest(name = "i64.rem_u({0}, {1}) = \"{2}\"")
	@CsvSource({
		"1,0,integer divide by zero",
		"0,0,integer divide by zero",
	})
	void rem_u_trap(@WastValue long firstParameter, @WastValue long secondParameter, String expectedTrapMessage) throws IOException {
		invokeTrap("rem_u", firstParameter, secondParameter, expectedTrapMessage);
	}

	@DisplayName("i64.rem_u")
	@ParameterizedTest(name = "i64.rem_u({0}, {1}) = {2}")
	@CsvSource({
		"1,1,0",
		"0,1,0",
		"-1,-1,0",
		"0x8000000000000000,-1,0x8000000000000000",
		"0x8000000000000000,2,0",
		"0x8ff00ff00ff00ff0,0x100000001,0x80000001",
		"0x8000000000000001,1000,809",
		"5,2,1",
		"-5,2,1",
		"5,-2,5",
		"-5,-2,-5",
		"7,3,1",
		"11,5,1",
		"17,7,3",
	})
	void rem_u(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("rem_u", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.and")
	@ParameterizedTest(name = "i64.and({0}, {1}) = {2}")
	@CsvSource({
		"1,0,0",
		"0,1,0",
		"1,1,1",
		"0,0,0",
		"0x7fffffffffffffff,0x8000000000000000,0",
		"0x7fffffffffffffff,-1,0x7fffffffffffffff",
		"0xf0f0ffff,0xfffff0f0,0xf0f0f0f0",
		"0xffffffffffffffff,0xffffffffffffffff,0xffffffffffffffff",
	})
	void and(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("and", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.or")
	@ParameterizedTest(name = "i64.or({0}, {1}) = {2}")
	@CsvSource({
		"1,0,1",
		"0,1,1",
		"1,1,1",
		"0,0,0",
		"0x7fffffffffffffff,0x8000000000000000,-1",
		"0x8000000000000000,0,0x8000000000000000",
		"0xf0f0ffff,0xfffff0f0,0xffffffff",
		"0xffffffffffffffff,0xffffffffffffffff,0xffffffffffffffff",
	})
	void or(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("or", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.xor")
	@ParameterizedTest(name = "i64.xor({0}, {1}) = {2}")
	@CsvSource({
		"1,0,1",
		"0,1,1",
		"1,1,0",
		"0,0,0",
		"0x7fffffffffffffff,0x8000000000000000,-1",
		"0x8000000000000000,0,0x8000000000000000",
		"-1,0x8000000000000000,0x7fffffffffffffff",
		"-1,0x7fffffffffffffff,0x8000000000000000",
		"0xf0f0ffff,0xfffff0f0,0x0f0f0f0f",
		"0xffffffffffffffff,0xffffffffffffffff,0",
	})
	void xor(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("xor", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.shl")
	@ParameterizedTest(name = "i64.shl({0}, {1}) = {2}")
	@CsvSource({
		"1,1,2",
		"1,0,1",
		"0x7fffffffffffffff,1,0xfffffffffffffffe",
		"0xffffffffffffffff,1,0xfffffffffffffffe",
		"0x8000000000000000,1,0",
		"0x4000000000000000,1,0x8000000000000000",
		"1,63,0x8000000000000000",
		"1,64,1",
		"1,65,2",
		"1,-1,0x8000000000000000",
		"1,0x7fffffffffffffff,0x8000000000000000",
	})
	void shl(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("shl", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.shr_s")
	@ParameterizedTest(name = "i64.shr_s({0}, {1}) = {2}")
	@CsvSource({
		"1,1,0",
		"1,0,1",
		"-1,1,-1",
		"0x7fffffffffffffff,1,0x3fffffffffffffff",
		"0x8000000000000000,1,0xc000000000000000",
		"0x4000000000000000,1,0x2000000000000000",
		"1,64,1",
		"1,65,0",
		"1,-1,0",
		"1,0x7fffffffffffffff,0",
		"1,0x8000000000000000,1",
		"0x8000000000000000,63,-1",
		"-1,64,-1",
		"-1,65,-1",
		"-1,-1,-1",
		"-1,0x7fffffffffffffff,-1",
		"-1,0x8000000000000000,-1",
	})
	void shr_s(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("shr_s", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.shr_u")
	@ParameterizedTest(name = "i64.shr_u({0}, {1}) = {2}")
	@CsvSource({
		"1,1,0",
		"1,0,1",
		"-1,1,0x7fffffffffffffff",
		"0x7fffffffffffffff,1,0x3fffffffffffffff",
		"0x8000000000000000,1,0x4000000000000000",
		"0x4000000000000000,1,0x2000000000000000",
		"1,64,1",
		"1,65,0",
		"1,-1,0",
		"1,0x7fffffffffffffff,0",
		"1,0x8000000000000000,1",
		"0x8000000000000000,63,1",
		"-1,64,-1",
		"-1,65,0x7fffffffffffffff",
		"-1,-1,1",
		"-1,0x7fffffffffffffff,1",
		"-1,0x8000000000000000,-1",
	})
	void shr_u(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("shr_u", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.rotl")
	@ParameterizedTest(name = "i64.rotl({0}, {1}) = {2}")
	@CsvSource({
		"1,1,2",
		"1,0,1",
		"-1,1,-1",
		"1,64,1",
		"0xabcd987602468ace,1,0x579b30ec048d159d",
		"0xfe000000dc000000,4,0xe000000dc000000f",
		"0xabcd1234ef567809,53,0x013579a2469deacf",
		"0xabd1234ef567809c,63,0x55e891a77ab3c04e",
		"0xabcd1234ef567809,0xf5,0x013579a2469deacf",
		"0xabcd7294ef567809,0xffffffffffffffed,0xcf013579ae529dea",
		"0xabd1234ef567809c,0x800000000000003f,0x55e891a77ab3c04e",
		"1,63,0x8000000000000000",
		"0x8000000000000000,1,1",
	})
	void rotl(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("rotl", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.rotr")
	@ParameterizedTest(name = "i64.rotr({0}, {1}) = {2}")
	@CsvSource({
		"1,1,0x8000000000000000",
		"1,0,1",
		"-1,1,-1",
		"1,64,1",
		"0xabcd987602468ace,1,0x55e6cc3b01234567",
		"0xfe000000dc000000,4,0x0fe000000dc00000",
		"0xabcd1234ef567809,53,0x6891a77ab3c04d5e",
		"0xabd1234ef567809c,63,0x57a2469deacf0139",
		"0xabcd1234ef567809,0xf5,0x6891a77ab3c04d5e",
		"0xabcd7294ef567809,0xffffffffffffffed,0x94a77ab3c04d5e6b",
		"0xabd1234ef567809c,0x800000000000003f,0x57a2469deacf0139",
		"1,63,2",
		"0x8000000000000000,63,1",
	})
	void rotr(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue long expectedResult) throws IOException {
		invoke("rotr", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.clz")
	@ParameterizedTest(name = "i64.clz({0}) = {1}")
	@CsvSource({
		"0xffffffffffffffff,0",
		"0,64",
		"0x00008000,48",
		"0xff,56",
		"0x8000000000000000,0",
		"1,63",
		"2,62",
		"0x7fffffffffffffff,1",
	})
	void clz(@WastValue long parameter, @WastValue long expectedResult) throws IOException {
		invoke("clz", parameter, expectedResult);
	}

	@DisplayName("i64.ctz")
	@ParameterizedTest(name = "i64.ctz({0}) = {1}")
	@CsvSource({
		"-1,0",
		"0,64",
		"0x00008000,15",
		"0x00010000,16",
		"0x8000000000000000,63",
		"0x7fffffffffffffff,0",
	})
	void ctz(@WastValue long parameter, @WastValue long expectedResult) throws IOException {
		invoke("ctz", parameter, expectedResult);
	}

	@DisplayName("i64.popcnt")
	@ParameterizedTest(name = "i64.popcnt({0}) = {1}")
	@CsvSource({
		"-1,64",
		"0,0",
		"0x00008000,1",
		"0x8000800080008000,4",
		"0x7fffffffffffffff,63",
		"0xAAAAAAAA55555555,32",
		"0x99999999AAAAAAAA,32",
		"0xDEADBEEFDEADBEEF,48",
	})
	void popcnt(@WastValue long parameter, @WastValue long expectedResult) throws IOException {
		invoke("popcnt", parameter, expectedResult);
	}

	@DisplayName("i64.extend8_s")
	@ParameterizedTest(name = "i64.extend8_s({0}) = {1}")
	@CsvSource({
		"0,0",
		"0x7f,127",
		"0x80,-128",
		"0xff,-1",
		"0x01234567_89abcd_00,0",
		"0xfedcba98_765432_80,-0x80",
		"-1,-1",
	})
	void extend8_s(@WastValue long parameter, @WastValue long expectedResult) throws IOException {
		invoke("extend8_s", parameter, expectedResult);
	}

	@DisplayName("i64.extend16_s")
	@ParameterizedTest(name = "i64.extend16_s({0}) = {1}")
	@CsvSource({
		"0,0",
		"0x7fff,32767",
		"0x8000,-32768",
		"0xffff,-1",
		"0x12345678_9abc_0000,0",
		"0xfedcba98_7654_8000,-0x8000",
		"-1,-1",
	})
	void extend16_s(@WastValue long parameter, @WastValue long expectedResult) throws IOException {
		invoke("extend16_s", parameter, expectedResult);
	}

	@DisplayName("i64.extend32_s")
	@ParameterizedTest(name = "i64.extend32_s({0}) = {1}")
	@CsvSource({
		"0,0",
		"0x7fff,32767",
		"0x8000,32768",
		"0xffff,65535",
		"0x7fffffff,0x7fffffff",
		"0x80000000,-0x80000000",
		"0xffffffff,-1",
		"0x01234567_00000000,0",
		"0xfedcba98_80000000,-0x80000000",
		"-1,-1",
	})
	void extend32_s(@WastValue long parameter, @WastValue long expectedResult) throws IOException {
		invoke("extend32_s", parameter, expectedResult);
	}

	@DisplayName("i64.eqz")
	@ParameterizedTest(name = "i64.eqz({0}) = {1}")
	@CsvSource({
		"0,1",
		"1,0",
		"0x8000000000000000,0",
		"0x7fffffffffffffff,0",
		"0xffffffffffffffff,0",
	})
	void eqz(@WastValue long parameter, @WastValue int expectedResult) throws IOException {
		invokeComparator("eqz", parameter, expectedResult);
	}

	@DisplayName("i64.eq")
	@ParameterizedTest(name = "i64.eq({0}) = {1}")
	@CsvSource({
		"0,0,1",
		"1,1,1",
		"-1,1,0",
		"0x8000000000000000,0x8000000000000000,1",
		"0x7fffffffffffffff,0x7fffffffffffffff,1",
		"-1,-1,1",
		"1,0,0",
		"0,1,0",
		"0x8000000000000000,0,0",
		"0,0x8000000000000000,0",
		"0x8000000000000000,-1,0",
		"-1,0x8000000000000000,0",
		"0x8000000000000000,0x7fffffffffffffff,0",
		"0x7fffffffffffffff,0x8000000000000000,0",
	})
	void eq(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue int expectedResult) throws IOException {
		invokeComparator("eq", firstParameter, secondParameter, expectedResult);
	}

	@DisplayName("i64.ne")
	@ParameterizedTest(name = "i64.ne({0}) = {1}")
	@CsvSource({
		"0,0,0",
		"1,1,0",
		"-1,1,1",
		"0x8000000000000000,0x8000000000000000,0",
		"0x7fffffffffffffff,0x7fffffffffffffff,0",
		"-1,-1,0",
		"1,0,1",
		"0,1,1",
		"0x8000000000000000,0,1",
		"0,0x8000000000000000,1",
		"0x8000000000000000,-1,1",
		"-1,0x8000000000000000,1",
		"0x8000000000000000,0x7fffffffffffffff,1",
		"0x7fffffffffffffff,0x8000000000000000,1",
	})
	void ne(@WastValue long firstParameter, @WastValue long secondParameter, @WastValue int expectedResult) throws IOException {
		invokeComparator("ne", firstParameter, secondParameter, expectedResult);
	}

	private static void invokeTrap(String functionName, long firstParameter, long secondParameter, String expectedTrapMessage) throws IOException {
		TrapException trapException = assertThrows(TrapException.class, () -> {
			invokeFunction(functionName, firstParameter, secondParameter);
		});
		assertEquals(expectedTrapMessage, trapException.getMessage());
	}

	private static void invokeComparator(String functionName, long parameter, int expectedResult) throws IOException {
		assertEquals(expectedResult, (int)invokeFunction(functionName, parameter));
	}

	private static void invokeComparator(String functionName, long firstParameter, long secondParameter, int expectedResult) throws IOException {
		assertEquals(expectedResult, (int)invokeFunction(functionName, firstParameter, secondParameter));
	}

	private static void invoke(String functionName, long parameter, long expectedResult) throws IOException {
		assertEquals(expectedResult, (long)invokeFunction(functionName, parameter));
	}

	private static void invoke(String functionName, long firstParameter, long secondParameter, long expectedResult) throws IOException {
		assertEquals(expectedResult, (long)invokeFunction(functionName, firstParameter, secondParameter));
	}

	private static <T> T invokeFunction(String functionName, Object... parameters) throws IOException {
		return createInterpreter("spec/i64/i64.0.wasm").invoke(functionName, parameters);
	}
}
