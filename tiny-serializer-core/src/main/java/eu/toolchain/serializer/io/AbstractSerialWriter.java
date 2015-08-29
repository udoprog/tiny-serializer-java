package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import eu.toolchain.serializer.types.CompactVarIntSerializer;

public abstract class AbstractSerialWriter implements SerialWriter {
    public static final CompactVarIntSerializer DEFAULT_SCOPE_SIZE = new CompactVarIntSerializer();

    private final SharedPool pool;
    protected final Serializer<Integer> scopeSize;

    public AbstractSerialWriter() {
        this(new ContiniousSharedPool(), DEFAULT_SCOPE_SIZE);
    }

    public AbstractSerialWriter(SharedPool pool, Serializer<Integer> scopeSize) {
        this.pool = pool;
        this.scopeSize = scopeSize;
    }

    @Override
    public void write(ByteBuffer buffer) throws IOException {
        write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        buffer.position(buffer.limit());
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
        return new ScopedSerialWriter(pool, scopeSize, this);
    }

    @Override
    public SharedPool pool() {
        return pool;
    }
}