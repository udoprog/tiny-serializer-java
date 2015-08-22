package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Test;

import eu.toolchain.serializer.types.IntegerSerializer;

public class TestIntegerSerializer {
    private void roundtrip(int value, int... pattern) throws IOException {
        Helpers.roundTripPattern(new IntegerSerializer(), value, pattern);
    }

    @Test
    public void testValidValues() throws IOException {
        roundtrip(0, 0x00, 0x00, 0x00, 0x00);
        roundtrip(-1, 0xff, 0xff, 0xff, 0xff);
        roundtrip(0xffff, 0x00, 0x00, 0xff, 0xff);
        roundtrip(-0x10000, 0xff, 0xff, 0x00, 0x00);
        roundtrip(Integer.MIN_VALUE, 0x80, 0x00, 0x00, 0x00);
        roundtrip(Integer.MAX_VALUE, 0x7f, 0xff, 0xff, 0xff);
    }
}
