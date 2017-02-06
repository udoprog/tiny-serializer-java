package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class ScopedSerialWriter extends AbstractSerialWriter implements SerialWriter.Scope {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private final SerialWriter parent;

    private long position = 0L;

    public ScopedSerialWriter(
        final SharedPool pool, final Serializer<Integer> scopeSize, final SerialWriter parent
    ) {
        super(pool, scopeSize);
        this.parent = parent;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public void write(byte b) throws IOException {
        output.write(b & 0xff);
        position += 1;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        output.write(bytes, offset, length);
        position += length;
    }

    @Override
    public void close() throws IOException {
        scopeSize.serialize(parent, output.size());
        parent.write(output.toByteArray());
    }
}
