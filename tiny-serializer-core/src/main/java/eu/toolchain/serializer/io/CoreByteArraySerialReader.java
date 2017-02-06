package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.EOFException;
import java.io.IOException;

public class CoreByteArraySerialReader extends AbstractSerialReader {
    private final byte[] source;

    private int position = 0;

    public CoreByteArraySerialReader(final byte[] source) {
        super();
        this.source = source;
    }

    public CoreByteArraySerialReader(
        final SharedPool pool, final Serializer<Integer> scopeSize, final byte[] source
    ) {
        super(pool, scopeSize);
        this.source = source;
    }

    @Override
    public long position() {
        return (long) position;
    }

    @Override
    public byte read() throws IOException {
        if (position + 1 > source.length) {
            throw new EOFException();
        }

        final byte b = source[position];
        position += 1;
        return b;
    }

    @Override
    public void read(byte[] bytes, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return;
        }

        if (position + length > source.length) {
            throw new EOFException();
        }

        System.arraycopy(source, position, bytes, offset, length);
        position += length;
    }

    @Override
    public void skip(int size) throws IOException {
        if (position + size > source.length) {
            throw new EOFException();
        }

        position += size;
    }
}
