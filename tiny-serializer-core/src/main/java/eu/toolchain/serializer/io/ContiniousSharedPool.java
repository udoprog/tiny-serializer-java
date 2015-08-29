package eu.toolchain.serializer.io;

import java.nio.ByteBuffer;

import eu.toolchain.serializer.SharedPool;

public class ContiniousSharedPool implements SharedPool {
    public static final int MIN_BUFFER_SIZE = 64;

    private int offset = 0;
    private int size = MIN_BUFFER_SIZE;
    private byte[] pool = null;

    @Override
    public ByteBuffer allocate(int size) {
        if (pool == null || pool.length < offset + size) {
            pool = new byte[next(offset + size)];
        }

        // we have enough space.
        final ByteBuffer result = ByteBuffer.wrap(pool, offset, size).slice();
        offset += size;
        return result;
    }

    private int next(int requested) {
        while (requested > size) {
            size = size * 2;
        }

        return size;
    }

    @Override
    public void release(int size) {
        offset = Math.max(0, offset - size);
    }
}