package eu.toolchain.serializer.io;

import java.nio.ByteBuffer;

/**
 * A dynamically growing ByteBuffer implementation.
 */
public class ByteBufferOutputStream {
    public static final int MIN_INITIAL_CAPACITY = 16;
    public static final int DEFAULT_CAPACITY = 32;
    public static final float DEFAULT_GROWTH_FACTOR = 1.6f;

    private ByteBuffer buffer;
    private final float growthFactor;

    public ByteBufferOutputStream() {
        this(DEFAULT_CAPACITY, DEFAULT_GROWTH_FACTOR);
    }

    public ByteBufferOutputStream(int initialCapacity) {
        this(initialCapacity, DEFAULT_GROWTH_FACTOR);
    }

    public ByteBufferOutputStream(int initialCapacity, float growthFactor) {
        if (growthFactor <= 1.0)
            throw new IllegalArgumentException("invalid growth factor, must be greater than 1.0");

        this.buffer = ByteBuffer.allocate(Math.max(initialCapacity, MIN_INITIAL_CAPACITY));
        this.growthFactor = growthFactor;
    }

    public void write(int b) {
        accomodate(1);
        buffer.put((byte) b);
    }

    public void write(byte[] bytes) {
        accomodate(bytes.length);
        buffer.put(bytes);
    }

    public ByteBuffer buffer() {
        final ByteBuffer result = buffer.asReadOnlyBuffer();
        result.flip();
        return result;
    }

    private void accomodate(int length) {
        if (buffer.remaining() >= length)
            return;

        int capacity = buffer.capacity();
        int used = buffer.position();

        while (capacity - used < length)
            capacity = (int) Math.ceil(capacity * growthFactor);

        final ByteBuffer original = this.buffer;
        original.flip();

        this.buffer = ByteBuffer.allocate(capacity);
        this.buffer.put(original);
    }
}
