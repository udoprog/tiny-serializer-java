package eu.toolchain.serializer.io;

import java.io.EOFException;
import java.io.IOException;

import eu.toolchain.serializer.Serializer;

public class CoreByteArraySerialReader extends AbstractSerialReader {
    private final byte[] source;

    private int p = 0;

    public CoreByteArraySerialReader(final byte[] source) {
        super();
        this.source = source;
    }

    public CoreByteArraySerialReader(final Serializer<Integer> scopeSize, final byte[] source) {
        super(scopeSize);
        this.source = source;
    }

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset + length > b.length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return;
        }

        if (p + length > source.length) {
            throw new EOFException();
        }

        System.arraycopy(source, p, b, offset, length);
        p += length;
    }

    @Override
    public void skip(int size) throws IOException {
        if (p + size > source.length) {
            throw new EOFException();
        }

        p += size;
    }
}