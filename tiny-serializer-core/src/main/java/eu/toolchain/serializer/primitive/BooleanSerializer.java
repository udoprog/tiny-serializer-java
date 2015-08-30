package eu.toolchain.serializer.primitive;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class BooleanSerializer implements Serializer<Boolean> {
    private static final byte TRUE[] = new byte[] { 0x1 };
    private static final byte FALSE[] = new byte[] { 0x0 };

    @Override
    public void serialize(SerialWriter buffer, Boolean value) throws IOException {
        buffer.write(value ? TRUE : FALSE);
    }

    @Override
    public Boolean deserialize(SerialReader buffer) throws IOException {
        final byte[] bytes = new byte[1];
        buffer.read(bytes);
        return bytes[0] == 0x1;
    }
}