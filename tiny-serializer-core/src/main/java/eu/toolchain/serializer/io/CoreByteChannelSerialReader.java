package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class CoreByteChannelSerialReader extends AbstractSerialReader {
    public static final int SKIP_SIZE = 1024;

    final ByteBuffer one = ByteBuffer.allocate(1);
    final ReadableByteChannel channel;

    long position = 0L;

    public CoreByteChannelSerialReader(
        final ReadableByteChannel channel
    ) {
        super();
        this.channel = channel;
    }

    public CoreByteChannelSerialReader(
        final SharedPool pool, final Serializer<Integer> scopeSize,
        final ReadableByteChannel channel
    ) {
        super(pool, scopeSize);
        this.channel = channel;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public byte read() throws IOException {
        channel.read(one);
        one.flip();
        final byte b = one.get();
        one.flip();

        position += 1;
        return b;
    }

    @Override
    public void read(byte[] bytes, int offset, int length) throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);

        while (buffer.remaining() > 0) {
            channel.read(buffer);
        }

        position += length;
    }

    @Override
    public void skip(final int length) throws IOException {
        final ByteBuffer skip = ByteBuffer.allocate(SKIP_SIZE);

        int skipped = 0;

        while (skipped < length) {
            skip.reset();
            skip.limit(Math.min(length - skipped, SKIP_SIZE));

            while (skip.remaining() > 0) {
                channel.read(skip);
            }

            skipped += skip.limit();
        }

        position += length;
    }
}
