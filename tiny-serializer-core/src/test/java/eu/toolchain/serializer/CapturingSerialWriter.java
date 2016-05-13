package eu.toolchain.serializer;

import eu.toolchain.serializer.io.AbstractSerialWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CapturingSerialWriter extends AbstractSerialWriter {
    final List<Integer> captured = new ArrayList<>();

    @Override
    public void write(byte b) throws IOException {
        captured.add((int) b);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        for (int i = offset; i < offset + length; i++) {
            captured.add((int) bytes[i]);
        }
    }

    public List<Integer> getCaptured() {
        return captured;
    }

    public SerialReader toSerialReader() {
        return new IteratorSerialReader(captured.iterator());
    }
}
