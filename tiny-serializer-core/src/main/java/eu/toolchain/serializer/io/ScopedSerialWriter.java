package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

class ScopedSerialWriter extends AbstractSerialWriter implements SerialWriter.Scope {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final SerialWriter parent;

    public ScopedSerialWriter(final Serializer<Integer> scopeSize, final SerialWriter parent) {
        super(scopeSize);
        this.parent = parent;
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
        scopeSize.serialize(parent, output.size());
        parent.write(output.toByteArray());
    }
}