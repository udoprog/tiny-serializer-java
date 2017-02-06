package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.IOException;

class ScopedSerialReader extends AbstractSerialReader {
    private final SerialReader parent;
    private final int size;

    private int position = 0;

    public ScopedSerialReader(
        final SharedPool pool, final Serializer<Integer> scopeSize, final SerialReader parent,
        final int size
    ) {
        super(pool, scopeSize);
        this.parent = parent;
        this.size = size;
    }

    @Override
    public long position() {
        return parent.position() + position;
    }

    @Override
    public byte read() throws IOException {
        retain(1);
        return parent.read();
    }

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        retain(b.length);
        parent.read(b, offset, length);
    }

    @Override
    public void skip(int length) throws IOException {
        retain(length);
        parent.skip(length);
    }

    private void retain(int requested) throws IOException {
        if (position + requested > size) {
            throw new IOException(
                "end-of-scope reached (p: " + position + ", size: " + size + ", requested: " +
                    requested + ")");
        }

        position += requested;
    }
}
