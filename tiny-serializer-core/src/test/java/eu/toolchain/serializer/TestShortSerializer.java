package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Test;

public class TestShortSerializer {
    private void roundtrip(short value, int... pattern) throws IOException {
        Helpers.roundtripArray(new ShortSerializer(), value, pattern);
    }

    @Test
    public void testValidValues() throws IOException {
        roundtrip((short) 0, 0x00, 0x00);
        roundtrip((short) -1, 0xff, 0xff);
        roundtrip((short) 0xff, 0x00, 0xff);
        roundtrip((short) -0x100, 0xff, 0x00);
        roundtrip(Short.MIN_VALUE, 0x80, 0x00);
        roundtrip(Short.MAX_VALUE, 0x7f, 0xff);
    }
}
