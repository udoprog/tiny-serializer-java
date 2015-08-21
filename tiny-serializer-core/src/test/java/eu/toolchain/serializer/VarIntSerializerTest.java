package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Test;

public class VarIntSerializerTest {
    final Serializer<Integer> s = new VarIntSerializer();

    private void roundtrip(int value, int... pattern) throws IOException {
        Helpers.verifyRoundtrip(s, value, pattern);
    }

    @Test
    public void testValidValues() throws IOException {
        roundtrip(0, 0x00);
        roundtrip(0x7f, 0x7f);
        roundtrip(0x80, 0x80, 0x01);
        roundtrip(0x3fff, 0xff, 0x7f);
        roundtrip(0x4000, 0x80, 0x80, 0x01);
        roundtrip(0x1fffff, 0xff, 0xff, 0x7f);
        roundtrip(0x200000, 0x80, 0x80, 0x80, 0x01);
        roundtrip(0xfffffff, 0xff, 0xff, 0xff, 0x7f);
        roundtrip(0x10000000, 0x80, 0x80, 0x80, 0x80, 0x01);
        roundtrip(Integer.MAX_VALUE, 0xff, 0xff, 0xff, 0xff, 0x07);
        roundtrip(Integer.MIN_VALUE, 0x80, 0x80, 0x80, 0x80, 0x08);
        roundtrip(-1, 0xff, 0xff, 0xff, 0xff, 0x0f);
    }
}