package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.types.CompactVarIntSerializer;

public class CoreBytesSerialWriter extends AbstractSerialWriter implements BytesSerialWriter {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public CoreBytesSerialWriter() {
        super();
    }

    public CoreBytesSerialWriter(final Serializer<Integer> scopeSize) {
        super(scopeSize);
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