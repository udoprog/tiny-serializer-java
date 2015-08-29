package eu.toolchain.serializer.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import eu.toolchain.serializer.Serializer;

public class CoreInputStreamSerialReader extends AbstractSerialReader {
    private final InputStream input;

    public CoreInputStreamSerialReader(final Serializer<Integer> scopeSize, final InputStream input) {
        super(scopeSize);
        this.input = input;
    }

    @Override
    public byte read() throws IOException {
        final int r = input.read();

        if (r == -1) {
            throw new EOFException();
        }

        return (byte) r;
    }

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        int index = 0;

        while (index < length) {
            final int r;

            if ((r = input.read(b, offset + index, length - index)) == -1) {
                throw new EOFException();
            }

            index += r;
        }
    }

    @Override
    public void skip(int length) throws IOException {
        int index = 0;

        while (index < length) {
            final long r;

            if ((r = input.skip(length - index)) == 0) {
                throw new EOFException();
            }

            index += r;
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}