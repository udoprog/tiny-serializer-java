package eu.toolchain.serializer.io;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.types.CompactVarIntSerializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSerialReader implements SerialReader {
    public static final CompactVarIntSerializer DEFAULT_SCOPE_SIZE = new CompactVarIntSerializer();

    protected final Serializer<Integer> scopeSize;

    public AbstractSerialReader() {
        this(DEFAULT_SCOPE_SIZE);
    }

    @Override
    public void read(byte[] bytes) throws IOException {
        read(bytes, 0, bytes.length);
    }

    @Override
    public void read(byte[] bytes, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return;
        }

        for (int i = 0; i < length; i++) {
            bytes[offset + i] = read();
        }
    }

    @Override
    public void skip() throws IOException {
        final int skip = scopeSize.deserialize(this);
        skip(skip);
    }

    @Override
    public SerialReader scope() throws IOException {
        final int size = scopeSize.deserialize(this);
        return new ScopedSerialReader(scopeSize, this, size);
    }

    @Override
    public void close() throws IOException {
    }
}