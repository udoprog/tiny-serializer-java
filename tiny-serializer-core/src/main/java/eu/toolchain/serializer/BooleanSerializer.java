package eu.toolchain.serializer;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class BooleanSerializer implements Serializer<Boolean> {
    private final int TRUE = 0x1;
    private final int FALSE = 0x0;

    @Override
    public void serialize(SerialWriter buffer, Boolean value) throws IOException {
        buffer.write(value ? TRUE : FALSE);
    }

    @Override
    public Boolean deserialize(SerialReader buffer) throws IOException {
        return buffer.read() == TRUE;
    }
}