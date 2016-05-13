package eu.toolchain.serializer;

import eu.toolchain.serializer.io.AbstractSerialReader;
import lombok.RequiredArgsConstructor;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class IteratorSerialReader extends AbstractSerialReader {
    final Iterator<Integer> iterator;

    @Override
    public byte read() throws IOException {
        try {
            return iterator.next().byteValue();
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
    }
}
