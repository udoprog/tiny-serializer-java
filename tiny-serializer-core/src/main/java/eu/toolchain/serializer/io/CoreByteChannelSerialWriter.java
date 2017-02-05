package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class CoreByteChannelSerialWriter extends AbstractSerialWriter {
    final ByteBuffer one = ByteBuffer.allocate(1);

    final WritableByteChannel channel;

    public CoreByteChannelSerialWriter(
        final SharedPool pool, final Serializer<Integer> scopeSize,
        final WritableByteChannel channel
    ) {
        super(pool, scopeSize);
        this.channel = channel;
    }

    @Override
    public void write(final ByteBuffer buffer) throws IOException {
        while (buffer.remaining() > 0) {
            channel.write(buffer);
        }
    }

    @Override
    public void write(byte b) throws IOException {
        one.reset();
        one.put(b);
        one.flip();
        write(one);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        write(ByteBuffer.wrap(bytes, offset, length));
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
