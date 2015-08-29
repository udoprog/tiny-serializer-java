package eu.toolchain.serializer.io;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

class ScopedSerialReader extends AbstractSerialReader {
    private final SerialReader parent;
    private final int size;

    private int p = 0;

    public ScopedSerialReader(final SharedPool pool, final Serializer<Integer> scopeSize, final SerialReader parent, final int size) {
        super(pool, scopeSize);
        this.parent = parent;
        this.size = size;
    }

    @Override
    public void read(byte[] b, int offset, int length) throws IOException {
        p += b.length;
        checkScope();
        parent.read(b, offset, length);
    }

    @Override
    public void skip(int length) throws IOException {
        p += length;
        checkScope();
        parent.skip(length);
    }

    private void checkScope() throws IOException {
        if (p > size) {
            throw new IOException("end of scope reached (p: " + p + ", size: " + size + ")");
        }
    }
}