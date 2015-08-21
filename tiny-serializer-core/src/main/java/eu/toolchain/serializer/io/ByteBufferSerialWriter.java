package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ByteBufferSerialWriter extends AbstractSerialWriter {
    private final ByteBufferOutputStream output = new ByteBufferOutputStream();

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
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Return a flipped, read-only reference to the underlying byte buffer.
     *
     * @return A new ByteBuffer for the current state of the buffer.
     */
    public ByteBuffer buffer() {
        return output.buffer();
    }
}
