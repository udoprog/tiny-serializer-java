package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

public class CoreByteChannelSerialWriter extends AbstractSerialWriter {
    final ByteBuffer singleByte = ByteBuffer.allocate(1);

    final WritableByteChannel channel;

    public CoreByteChannelSerialWriter(final SharedPool pool, final Serializer<Integer> scopeSize, final WritableByteChannel channel) {
        super(pool, scopeSize);
        this.channel = channel;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        channel.write(ByteBuffer.wrap(bytes, offset, length));
    }
}