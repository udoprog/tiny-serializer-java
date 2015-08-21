package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Test;

public class CompactVarIntSerializerTest {
    final Serializer<Integer> s = new CompactVarIntSerializer();

    private void roundtrip(int value, int... pattern) throws IOException {
        Helpers.verifyRoundtrip(s, value, pattern);
    }

    @Test
    public void testValidValues() throws IOException {
        roundtrip(0, 0x00);
        roundtrip(0x7f, 0x7f);

        roundtrip(0x80, 0x80, 0x00);
        roundtrip(0x407f, 0xff, 0x7f);

        roundtrip(0x4080, 0x80, 0x80, 0x00);
        roundtrip(0x20407f, 0xff, 0xff, 0x7f);

        roundtrip(0x204080, 0x80, 0x80, 0x80, 0x00);
        roundtrip(0x1020407f, 0xff, 0xff, 0xff, 0x7f);

        roundtrip(0x10204080, 0x80, 0x80, 0x80, 0x80, 0x00);
        roundtrip(Integer.MAX_VALUE, 0xff, 0xfe, 0xfe, 0xfe, 0x06);

        roundtrip(Integer.MIN_VALUE, 0x80, 0xff, 0xfe, 0xfe, 0x06);
        roundtrip(-1, 0xff, 0xfe, 0xfe, 0xfe, 0x0e);
    }
}