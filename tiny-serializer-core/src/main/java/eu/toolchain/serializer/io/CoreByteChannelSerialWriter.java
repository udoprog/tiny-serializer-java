package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class CoreByteChannelSerialWriter extends AbstractSerialWriter {
    final ByteBuffer one = ByteBuffer.wrap(new byte[1], 0, 1);

    final WritableByteChannel channel;

    long position = 0L;

    public CoreByteChannelSerialWriter(
        final WritableByteChannel channel
    ) {
        super();
        this.channel = channel;
    }

    public CoreByteChannelSerialWriter(
        final SharedPool pool, final Serializer<Integer> scopeSize,
        final WritableByteChannel channel
    ) {
        super(pool, scopeSize);
        this.channel = channel;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public void write(final ByteBuffer buffer) throws IOException {
        final int length = buffer.remaining();

        while (buffer.remaining() > 0) {
            channel.write(buffer);
        }

        position += length;
    }

    @Override
    public void write(byte b) throws IOException {
        one.position(0);
        one.put(b).flip();
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
