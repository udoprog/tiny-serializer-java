package eu.toolchain.serializer.primitive;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class LongSerializer implements Serializer<Long> {
    public static final int BYTES = 8;

    @Override
    public void serialize(SerialWriter buffer, Long value) throws IOException {
        final byte[] bytes = new byte[BYTES];
        toBytes(value, bytes, 0);
        buffer.write(bytes);
    }

    @Override
    public Long deserialize(SerialReader buffer) throws IOException {
        final byte[] b = new byte[BYTES];
        buffer.read(b);
        return fromBytes(b, 0);
    }

    public static void toBytes(long v, byte[] b, int o) {
        b[o + 0] = (byte) (v >> 56);
        b[o + 1] = (byte) (v >>> 48);
        b[o + 2] = (byte) (v >>> 40);
        b[o + 3] = (byte) (v >>> 32);
        b[o + 4] = (byte) (v >>> 24);
        b[o + 5] = (byte) (v >>> 16);
        b[o + 6] = (byte) (v >>> 8);
        b[o + 7] = (byte) (v);
    }

    public static long fromBytes(final byte[] b, int o) {
        long v = 0;

        v += ((long)(b[o + 0] & 0xff) << 56);
        v += ((long)(b[o + 1] & 0xff) << 48);
        v += ((long)(b[o + 2] & 0xff) << 40);
        v += ((long)(b[o + 3] & 0xff) << 32);
        v += ((long)(b[o + 4] & 0xff) << 24);
        v += ((long)(b[o + 5] & 0xff) << 16);
        v += ((long)(b[o + 6] & 0xff) << 8);
        v += ((long)(b[o + 7] & 0xff));

        return v;
    }
}