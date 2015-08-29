package eu.toolchain.serializer.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import eu.toolchain.serializer.Serializer;

public class CoreByteChannelSerialReader extends AbstractSerialReader {
    public static final int SKIP_SIZE = 1024;

    final ReadableByteChannel channel;

    public CoreByteChannelSerialReader(final Serializer<Integer> scopeSize, final ReadableByteChannel channel) {
        super(scopeSize);
        this.channel = channel;
    }

    @Override
    public void read(byte[] bytes, int offset, int length) throws IOException {
        channel.read(ByteBuffer.wrap(bytes, offset, length));
    }

    @Override
    public void skip(final int length) throws IOException {
        final ByteBuffer skip = ByteBuffer.allocate(SKIP_SIZE);

        int skipped = 0;

        while (skipped < length) {
            final ByteBuffer skipper = skip.asReadOnlyBuffer();
            final int current = Math.min(length - skipped, SKIP_SIZE);
            skipper.limit(current);
            channel.read(skipper);
            skipped += current;
        }
    }
}