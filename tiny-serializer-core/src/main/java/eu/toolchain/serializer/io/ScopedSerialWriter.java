package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

class ScopedSerialWriter extends AbstractSerialWriter implements SerialWriter.Scope {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final SerialWriter parent;

    public ScopedSerialWriter(final SharedPool pool, final Serializer<Integer> scopeSize, final SerialWriter parent) {
        super(pool, scopeSize);
        this.parent = parent;
    }

    @Override
    public void write(byte b) throws IOException {
        output.write(b & 0xff);
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