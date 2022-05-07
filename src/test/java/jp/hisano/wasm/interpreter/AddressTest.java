package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("address.wast")
class AddressTest {
	@DisplayName("i32.load*")
	@ParameterizedTest(name = "{0}({1}) = {2}")
	@CsvSource({
		"8u_good1,0,97",
		"8u_good2,0,97",
		"8u_good3,0,98",
		"8u_good4,0,99",
		"8u_good5,0,122",
		"8s_good1,0,97",
		"8s_good2,0,97",
		"8s_good3,0,98",
		"8s_good4,0,99",
		"8s_good5,0,122",
		"16u_good1,0,25185",
		"16u_good2,0,25185",
		"16u_good3,0,25442",
		"16u_good4,0,25699",
		"16u_good5,0,122",
		"16s_good1,0,25185",
		"16s_good2,0,25185",
		"16s_good3,0,25442",
		"16s_good4,0,25699",
		"16s_good5,0,122",
		"32_good1,0,1684234849",
		"32_good2,0,1684234849",
		"32_good3,0,1701077858",
		"32_good4,0,1717920867",
		"32_good5,0,122",
		"8u_good1,65507,0",
		"8u_good2,65507,0",
		"8u_good3,65507,0",
		"8u_good4,65507,0",
		"8u_good5,65507,0",
		"8s_good1,65507,0",
		"8s_good2,65507,0",
		"8s_good3,65507,0",
		"8s_good4,65507,0",
		"8s_good5,65507,0",
		"16u_good1,65507,0",
		"16u_good2,65507,0",
		"16u_good3,65507,0",
		"16u_good4,65507,0",
		"16u_good5,65507,0",
		"16s_good1,65507,0",
		"16s_good2,65507,0",
		"16s_good3,65507,0",
		"16s_good4,65507,0",
		"16s_good5,65507,0",
		"32_good1,65507,0",
		"32_good2,65507,0",
		"32_good3,65507,0",
		"32_good4,65507,0",
		"32_good5,65507,0",
		"8u_good1,65508,0",
		"8u_good2,65508,0",
		"8u_good3,65508,0",
		"8u_good4,65508,0",
		"8u_good5,65508,0",
		"8s_good1,65508,0",
		"8s_good2,65508,0",
		"8s_good3,65508,0",
		"8s_good4,65508,0",
		"8s_good5,65508,0",
		"16u_good1,65508,0",
		"16u_good2,65508,0",
		"16u_good3,65508,0",
		"16u_good4,65508,0",
		"16u_good5,65508,0",
		"16s_good1,65508,0",
		"16s_good2,65508,0",
		"16s_good3,65508,0",
		"16s_good4,65508,0",
		"16s_good5,65508,0",
		"32_good1,65508,0",
		"32_good2,65508,0",
		"32_good3,65508,0",
		"32_good4,65508,0",
	})
	void load(String functionName, int address, int expectedValue) throws IOException {
		int resultValue = createInterpreter("spec/address/address.0.wasm").invoke(functionName, address);
		assertEquals(expectedValue, resultValue);
	}
}
