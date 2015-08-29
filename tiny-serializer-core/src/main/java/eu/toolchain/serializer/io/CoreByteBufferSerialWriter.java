package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.Serializer;

public class CoreByteBufferSerialWriter extends AbstractSerialWriter {
    private final ByteBuffer buffer;

    public CoreByteBufferSerialWriter(final Serializer<Integer> scopeSize, final ByteBuffer buffer) {
        super(scopeSize);
        this.buffer = buffer;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        buffer.put(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        buffer.put(bytes, offset, length);
    }

    @Override
    public void close() throws IOException {
        super.close();
        buffer.flip();
    }
}