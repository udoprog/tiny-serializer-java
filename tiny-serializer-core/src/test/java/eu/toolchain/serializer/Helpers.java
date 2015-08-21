package eu.toolchain.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;

import eu.toolchain.serializer.io.OutputStreamSerialWriter;

public final class Helpers {
    public static <T> CapturingSerialWriter roundtrip(Serializer<T> serializer, T value) throws IOException {
        final CapturingSerialWriter out = new CapturingSerialWriter();
        serializer.serialize(out, value);
        final T result = serializer.deserialize(out.toSerialReader());
        Assert.assertEquals(value, result);
        return out;
    }

    public static <T> void roundTripPattern(Serializer<T> serializer, T value, final int[] pattern) throws IOException {
        final CapturingSerialWriter out = roundtrip(serializer, value);

        for (int i = 0; i < pattern.length; ++i) {
            final Integer b = out.getCaptured().get(i);
            Assert.assertEquals(String.format("pattern position#%d should match", i), (byte) pattern[i],
                    (byte) b.intValue());
        }
    }

    public static <T> byte[] serialize(Serializer<T> s, T value) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        s.serialize(new OutputStreamSerialWriter(out), value);
        return out.toByteArray();
    }
}
