package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class CoreInputStreamSerialReader extends AbstractSerialReader {
    private final InputStream input;

    public CoreInputStreamSerialReader(final InputStream input) {
        super();
        this.input = input;
    }

    public CoreInputStreamSerialReader(
        final SharedPool pool, final Serializer<Integer> scopeSize, final InputStream input
    ) {
        super(pool, scopeSize);
        this.input = input;
    }

    @Override
    public byte read() throws IOException {
        final int b = input.read();

        if (b < 0) {
            throw new EOFException();
        }

        return (byte) b;
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
