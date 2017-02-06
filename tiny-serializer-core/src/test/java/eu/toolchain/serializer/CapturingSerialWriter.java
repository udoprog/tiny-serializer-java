package eu.toolchain.serializer;

import eu.toolchain.serializer.io.AbstractSerialWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapturingSerialWriter extends AbstractSerialWriter {
    final List<Integer> captured = new ArrayList<>();

    long position = 0L;

    @Override
    public long position() {
        return position;
    }

    @Override
    public void write(byte b) throws IOException {
        captured.add((int) b);
        position += 1;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        for (int i = offset; i < offset + length; i++) {
            captured.add((int) bytes[i]);
        }

        position += length;
    }

    public List<Integer> getCaptured() {
        return captured;
    }

    public SerialReader toSerialReader() {
        return new IteratorSerialReader(captured.iterator());
    }
}
