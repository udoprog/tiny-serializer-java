package eu.toolchain.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.toolchain.serializer.io.AbstractSerialWriter;

public class CapturingSerialWriter extends AbstractSerialWriter {
    final List<Integer> captured = new ArrayList<>();

    public CapturingSerialWriter() {
        super(null);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        for (int i = offset; i < offset + length; i++) {
            captured.add((int)bytes[i]);
        }
    }

    public List<Integer> getCaptured() {
        return captured;
    }

    public SerialReader toSerialReader() {
        return new IteratorSerialReader(captured.iterator());
    }
}