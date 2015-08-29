package eu.toolchain.serializer.io;

import java.io.IOException;
import java.io.OutputStream;

import eu.toolchain.serializer.Serializer;

public class CoreOutputStreamSerialWriter extends AbstractSerialWriter implements StreamSerialWriter {
    private final OutputStream output;

    public CoreOutputStreamSerialWriter(final OutputStream output) {
        super();
        this.output = output;
    }

    public CoreOutputStreamSerialWriter(final Serializer<Integer> scopeSize, final OutputStream output) {
        super(scopeSize);
        this.output = output;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        output.write(bytes, offset, length);
    }

    @Override
    public void close() throws IOException {
        output.close();
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }
}