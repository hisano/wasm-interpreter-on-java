package jp.hisano.wasm.interpreter;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static jp.hisano.wasm.interpreter.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("i64.load instruction")
class I64LoadTest {
    @Test
    @DisplayName("i64.load should read a 64-bit value from memory")
    void testI64Load() throws IOException {
        // Test the readInt64 method of Memory class
        Memory memory = new Memory(1, 1);
        
        // Set up memory with test values (0x0102030405060708)
        byte[] testData = new byte[] {0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01};
        memory.setData(0, testData);
        
        // Test readInt64 directly
        long value = memory.readInt64(0);
        assertEquals(0x0102030405060708L, value, "Memory readInt64 should read the correct value");
        
        // Test the I64Load instruction directly
        Module.I64Load i64Load = new Module.I64Load(3, 0); // align=3, offset=0
        Value result = i64Load.readMemory(memory, 0);
        
        assertEquals(0x0102030405060708L, result.getI64(), "I64Load should read the correct 64-bit value");
    }
}