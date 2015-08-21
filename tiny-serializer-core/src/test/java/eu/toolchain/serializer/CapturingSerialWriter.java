package eu.toolchain.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import eu.toolchain.serializer.io.AbstractSerialWriter;

@RequiredArgsConstructor
public class CapturingSerialWriter extends AbstractSerialWriter {
    final List<Integer> captured = new ArrayList<>();

    @Override
    public void write(int b) throws IOException {
        captured.add(b);
    }

    @Override
    public void flush() throws IOException {
    }

    public List<Integer> getCaptured() {
        return captured;
    }

    public SerialReader toSerialReader() {
        return new IteratorSerialReader(captured.iterator());
    }
}