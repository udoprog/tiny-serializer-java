package eu.toolchain.serializer;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import eu.toolchain.serializer.io.AbstractSerialWriter;

@RequiredArgsConstructor
public class CapturingSerialWriter extends AbstractSerialWriter {
    final List<Integer> captured;

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        int i = offset;

        while (i < length) {
            write(bytes[i++]);
        }
    }

    @Override
    public void write(int b) throws IOException {
        captured.add(b);
    }

    @Override
    public void flush() throws IOException {
    }
}