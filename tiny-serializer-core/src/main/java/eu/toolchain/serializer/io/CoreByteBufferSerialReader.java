package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

public class CoreByteBufferSerialReader extends AbstractSerialReader {
    private final ByteBuffer buffer;

    public CoreByteBufferSerialReader(final SharedPool pool, final Serializer<Integer> scopeSize, final ByteBuffer buffer) {
        super(pool, scopeSize);
        this.buffer = buffer.asReadOnlyBuffer();
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