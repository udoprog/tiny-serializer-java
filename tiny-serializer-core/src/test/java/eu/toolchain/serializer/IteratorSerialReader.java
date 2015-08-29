package eu.toolchain.serializer;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import eu.toolchain.serializer.io.AbstractSerialReader;

@RequiredArgsConstructor
public class IteratorSerialReader extends AbstractSerialReader {
    final Iterator<Integer> iterator;

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        int i = offset;

        while (i < offset + length) {
            b[i++] = iterator.next().byteValue();
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