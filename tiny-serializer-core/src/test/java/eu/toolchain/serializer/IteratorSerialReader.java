package eu.toolchain.serializer;

import eu.toolchain.serializer.io.AbstractSerialReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IteratorSerialReader extends AbstractSerialReader {
    final Iterator<Integer> iterator;

    long position = 0L;

    @Override
    public long position() {
        return position;
    }

    @Override
    public byte read() throws IOException {
        try {
            final byte b = iterator.next().byteValue();
            position += 1;
            return b;
        } catch (NoSuchElementException e) {
            throw new EOFException(String.format("End of iterator"));
        }
    }

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        int i = offset;

        while (i < offset + length) {
            try {
                b[i++] = iterator.next().byteValue();
            } catch (NoSuchElementException e) {
                throw new EOFException(String.format("End of iterator on offset #%d", i));
            }
        }

        position += length;
    }

    @Override
    public void skip(int length) throws IOException {
        int i = 0;

        while (i++ < length) {
            try {
                iterator.next();
            } catch (NoSuchElementException e) {
                throw new EOFException();
            }
        }

        position += length;
    }
}
