package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.BytesSerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

public class CoreBytesSerialWriter extends AbstractSerialWriter implements BytesSerialWriter {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public CoreBytesSerialWriter() {
        super();
    }

    public CoreBytesSerialWriter(final SharedPool pool, final Serializer<Integer> scopeSize) {
        super(pool, scopeSize);
    }

    @Override
    public void write(byte b) {
        output.write(b & 0xff);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        output.write(bytes, offset, length);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public byte[] toByteArray() {
        return output.toByteArray();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toByteArray());
    }
}