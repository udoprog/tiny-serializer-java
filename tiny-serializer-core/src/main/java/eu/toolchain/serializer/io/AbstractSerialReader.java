package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import eu.toolchain.serializer.primitive.CompactVarIntSerializer;

public abstract class AbstractSerialReader implements SerialReader {
    public static final CompactVarIntSerializer DEFAULT_SCOPE_SIZE = new CompactVarIntSerializer();

    private final SharedPool pool;
    protected final Serializer<Integer> scopeSize;

    public AbstractSerialReader() {
        this(new ContinuousSharedPool(), DEFAULT_SCOPE_SIZE);
    }

    public AbstractSerialReader(SharedPool pool, Serializer<Integer> scopeSize) {
        this.pool = pool;
        this.scopeSize = scopeSize;
    }

    @Override
    public void read(ByteBuffer bytes) throws IOException {
        read(bytes.array(), bytes.arrayOffset(), bytes.remaining());
        bytes.position(bytes.remaining());
    }

    @Override
    public void read(byte[] bytes) throws IOException {
        read(bytes, 0, bytes.length);
    }

    @Override
    public void skip() throws IOException {
        final int skip = scopeSize.deserialize(this);
        skip(skip);
    }

    @Override
    public SerialReader scope() throws IOException {
        final int size = scopeSize.deserialize(this);
        return new ScopedSerialReader(pool, scopeSize, this, size);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public SharedPool pool() {
        return pool;
    }
}