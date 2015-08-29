package eu.toolchain.serializer.io;

import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractBasicSerialWriter implements SerialWriter {
    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        for (int i = 0; i < bytes.length; i++) {
            write(bytes[i]);
        }
    }

    @Override
    public void close() throws IOException {
    }
}