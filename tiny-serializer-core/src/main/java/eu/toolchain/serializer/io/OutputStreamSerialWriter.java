package eu.toolchain.serializer.io;

import java.io.IOException;
import java.io.OutputStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OutputStreamSerialWriter extends AbstractSerialWriter {
    private final OutputStream output;

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        output.write(bytes, offset, length);
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }
}