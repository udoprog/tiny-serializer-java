package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

public class CoreByteBufferSerialWriter extends AbstractSerialWriter {
    private final ByteBuffer buffer;

    public CoreByteBufferSerialWriter(final SharedPool pool, final Serializer<Integer> scopeSize, final ByteBuffer buffer) {
        super(pool, scopeSize);
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