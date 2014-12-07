package eu.toolchain.serializer;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class IntegerSerializer implements Serializer<Integer> {
    @Override
    public void serialize(SerialWriter buffer, Integer value) throws IOException {
        int v = value;

        // @formatter:off
        buffer.write(new byte[] {
            (byte) (v >>> 24),
            (byte) (v >>> 16),
            (byte) (v >>> 8),
            (byte) (v)
        });
        // @formatter:on
    }

    @Override
    public Integer deserialize(SerialReader buffer) throws IOException {
        final byte[] b = new byte[4];
        buffer.read(b);

        // @formatter:off
        return (int) (
            ((b[0] & 0xff) << 24)
          + ((b[1] & 0xff) << 16)
          + ((b[2] & 0xff) << 8)
          + ((b[3] & 0xff))
        );
        // @formatter:on
    }
}