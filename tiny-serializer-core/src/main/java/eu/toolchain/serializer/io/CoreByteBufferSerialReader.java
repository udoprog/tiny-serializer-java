package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.Serializer;

public class CoreByteBufferSerialReader extends AbstractSerialReader {
    private final ByteBuffer buffer;

    public CoreByteBufferSerialReader(final Serializer<Integer> scopeSize, final ByteBuffer buffer) {
        super(scopeSize);
        this.buffer = buffer.asReadOnlyBuffer();
    }

    @Override
    public byte read() throws IOException {
        return buffer.get();
    }

    @Override
    public void read(byte[] bytes, int offset, int length) throws IOException {
        buffer.get(bytes, offset, length);
    }

    @Override
    public void skip(int length) throws IOException {
        buffer.position(buffer.position() + length);
    }
}