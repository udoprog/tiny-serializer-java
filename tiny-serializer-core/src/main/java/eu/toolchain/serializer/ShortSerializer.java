package eu.toolchain.serializer;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class ShortSerializer implements Serializer<Short> {
    @Override
    public void serialize(SerialWriter buffer, Short value) throws IOException {
        short v = value;

        // @formatter:off
        buffer.write(new byte[] {
            (byte) (v >>> 8),
            (byte) (v)
        });
        // @formatter:on
    }

    @Override
    public Short deserialize(SerialReader buffer) throws IOException {
        final byte[] b = new byte[2];
        buffer.read(b);

        // @formatter:off
        return (short) (
          + ((b[0] & 0xff) << 8)
          + ((b[1] & 0xff))
        );
        // @formatter:on
    }
}