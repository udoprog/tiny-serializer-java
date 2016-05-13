package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SharedPool;

import java.nio.ByteBuffer;

public class ImmediateSharedPool implements SharedPool {
    @Override
    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size);
    }

    @Override
    public void release(int size) {
    }
}
