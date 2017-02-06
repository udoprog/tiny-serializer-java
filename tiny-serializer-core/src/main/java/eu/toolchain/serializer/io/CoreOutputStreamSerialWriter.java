package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import eu.toolchain.serializer.StreamSerialWriter;
import java.io.IOException;
import java.io.OutputStream;

public class CoreOutputStreamSerialWriter extends AbstractSerialWriter
    implements StreamSerialWriter {
    private final OutputStream output;

    private long position = 0L;

    public CoreOutputStreamSerialWriter(final OutputStream output) {
        super();
        this.output = output;
    }

    public CoreOutputStreamSerialWriter(
        final SharedPool pool, final Serializer<Integer> scopeSize, final OutputStream output
    ) {
        super(pool, scopeSize);
        this.output = output;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public void write(byte b) throws IOException {
        output.write(b & 0xff);
        position += 1;
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        output.write(bytes, offset, length);
        position += length;
    }

    @Override
    public void close() throws IOException {
        output.close();
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }
}
