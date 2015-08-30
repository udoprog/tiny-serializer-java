package eu.toolchain.serializer.primitive;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class ByteSerializer implements Serializer<Byte> {
    @Override
    public void serialize(SerialWriter buffer, Byte value) throws IOException {
        buffer.write(new byte[] { value });
    }

    @Override
    public Byte deserialize(SerialReader buffer) throws IOException {
        final byte[] bytes = new byte[1];
        buffer.read(bytes);
        return bytes[0];
    }
}