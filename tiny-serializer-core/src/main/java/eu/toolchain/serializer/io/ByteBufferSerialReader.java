package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ByteBufferSerialReader extends AbstractSerialReader {
    private final ByteBuffer buffer;

    @Override
    public byte read() throws IOException {
        return buffer.get();
    }

    @Override
    public void read(byte[] bytes) throws IOException {
        buffer.get(bytes);
    }

    @Override
    public void skip(int length) throws IOException {
        buffer.position(buffer.position() + length);
    }
}