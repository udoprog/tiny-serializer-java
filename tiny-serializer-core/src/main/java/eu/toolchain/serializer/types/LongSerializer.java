package eu.toolchain.serializer.types;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class LongSerializer implements Serializer<Long> {
    @Override
    public void serialize(SerialWriter buffer, Long value) throws IOException {
        long v = value;

        // @formatter:off
        buffer.write(new byte[] {
            (byte) (v >> 56),
            (byte) (v >>> 48),
            (byte) (v >>> 40),
            (byte) (v >>> 32),
            (byte) (v >>> 24),
            (byte) (v >>> 16),
            (byte) (v >>> 8),
            (byte) (v)
        });
        // @formatter:on
    }

    @Override
    public Long deserialize(SerialReader buffer) throws IOException {
        final byte[] b = new byte[8];
        buffer.read(b);

        // @formatter:off
        return (long) (
            ((long)(b[0] & 0xff) << 56)
          + ((long)(b[1] & 0xff) << 48)
          + ((long)(b[2] & 0xff) << 40)
          + ((long)(b[3] & 0xff) << 32)
          + ((long)(b[4] & 0xff) << 24)
          + ((long)(b[5] & 0xff) << 16)
          + ((long)(b[6] & 0xff) << 8)
          + ((long)(b[7] & 0xff))
        );
        // @formatter:on
    }
}