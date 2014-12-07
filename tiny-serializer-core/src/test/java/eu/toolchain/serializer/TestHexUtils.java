package eu.toolchain.serializer;

import org.junit.Assert;
import org.junit.Test;

public class TestHexUtils {
    private void roundtrip(String reference, int... bytes) {
        byte[] b = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++)
            b[i] = (byte) (bytes[i] & 0xff);

        Assert.assertEquals(reference, HexUtils.toHex(b));
    }

    @Test
    public void test() {
        roundtrip("0x");
        roundtrip("0x0102a1a2f1f2", 0x01, 0x02, 0xa1, 0xa2, 0xf1, 0xf2);
    }
}
