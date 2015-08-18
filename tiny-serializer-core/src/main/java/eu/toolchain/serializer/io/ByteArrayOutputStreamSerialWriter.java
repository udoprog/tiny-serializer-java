package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteArrayOutputStreamSerialWriter extends AbstractSerialWriter {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        output.write(bytes, offset, length);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    public byte[] toByteArray() {
        return output.toByteArray();
    }
}