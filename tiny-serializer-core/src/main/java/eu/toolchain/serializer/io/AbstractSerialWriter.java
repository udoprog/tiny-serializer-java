package eu.toolchain.serializer.io;

import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.types.CompactVarIntSerializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSerialWriter implements SerialWriter {
    public static final CompactVarIntSerializer DEFAULT_SCOPE_SIZE = new CompactVarIntSerializer();

    protected final Serializer<Integer> scopeSize;

    public AbstractSerialWriter() {
        this(DEFAULT_SCOPE_SIZE);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public SerialWriter.Scope scope() {
        return new ScopedSerialWriter(scopeSize, this);
    }
}