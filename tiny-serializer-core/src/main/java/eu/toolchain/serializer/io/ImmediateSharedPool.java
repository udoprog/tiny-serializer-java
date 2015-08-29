package eu.toolchain.serializer.io;

import java.nio.ByteBuffer;

import eu.toolchain.serializer.SharedPool;

public class ImmediateSharedPool implements SharedPool {
    @Override
    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocate(size);
    }

    @Override
    public void release(int size) {
    }
}