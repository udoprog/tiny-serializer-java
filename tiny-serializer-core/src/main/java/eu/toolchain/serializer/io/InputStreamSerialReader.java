package eu.toolchain.serializer.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InputStreamSerialReader extends AbstractSerialReader {
    private final InputStream input;

    @Override
    public byte read() throws IOException {
        final int r = input.read();

        if (r == -1) {
            throw new EOFException();
        }

        return (byte) r;
    }

    @Override
    public void read(byte[] b) throws IOException {
        int index = 0;

        while (index < b.length) {
            final int r;

            if ((r = input.read(b, index, b.length - index)) == -1) {
                throw new EOFException();
            }

            index += r;
        }
    }

    @Override
    public void skip(int length) throws IOException {
        input.skip(length);
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}