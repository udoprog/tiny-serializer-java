package eu.toolchain.serializer;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import eu.toolchain.serializer.io.AbstractSerialReader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IteratorSerialReader extends AbstractSerialReader {
    final Iterator<Integer> iterator;

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        int i = offset;

        while (i < offset + length) {
            try {
                b[i++] = iterator.next().byteValue();
            } catch(NoSuchElementException e) {
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