package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

import java.io.IOException;

public class IntegerSerializer implements Serializer<Integer> {
    public static final int BYTES = 4;

    @Override
    public void serialize(SerialWriter buffer, Integer value) throws IOException {
        final byte[] bytes = new byte[BYTES];
        toBytes(value, bytes, 0);
        buffer.write(bytes);
    }

    @Override
    public Integer deserialize(SerialReader buffer) throws IOException {
        final byte[] b = new byte[BYTES];
        buffer.read(b);
        return fromBytes(b, 0);
    }

    public static void toBytes(long v, byte[] b, int o) {
        b[o + 0] = (byte) (v >>> 24);
        b[o + 1] = (byte) (v >>> 16);
        b[o + 2] = (byte) (v >>> 8);
        b[o + 3] = (byte) (v);
    }

    public static int fromBytes(final byte[] b, int o) {
        int v = 0;

        v += ((int) (b[o + 0] & 0xff) << 24);
        v += ((int) (b[o + 1] & 0xff) << 16);
        v += ((int) (b[o + 2] & 0xff) << 8);
        v += ((int) (b[o + 3] & 0xff));

        return v;
    }
}
