package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OutputStreamSerialWriter extends AbstractSerialWriter {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        output.write(bytes);
    }

    public byte[] toByteArray() {
        return output.toByteArray();
    }
}
