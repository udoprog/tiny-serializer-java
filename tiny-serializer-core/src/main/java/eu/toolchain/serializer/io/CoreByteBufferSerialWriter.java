package eu.toolchain.serializer.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
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
    public void write(byte b) throws IOException {
        try {
            buffer.put(b);
        } catch (final BufferOverflowException e) {
            throw new EOFException();
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        try {
            buffer.put(bytes);
        } catch (final BufferOverflowException e) {
            throw new EOFException();
        }
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        try {
            buffer.put(bytes, offset, length);
        } catch (final BufferOverflowException e) {
            throw new EOFException();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        buffer.flip();
    }
}