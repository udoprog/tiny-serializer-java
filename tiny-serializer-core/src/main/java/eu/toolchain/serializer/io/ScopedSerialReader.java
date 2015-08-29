package eu.toolchain.serializer.io;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;

class ScopedSerialReader extends AbstractSerialReader {
    private final SerialReader parent;
    private final int size;

    private int p = 0;

    public ScopedSerialReader(final Serializer<Integer> scopeSize, final SerialReader parent, final int size) {
        super(scopeSize);
        this.parent = parent;
        this.size = size;
    }

    @Override
    public byte read() throws IOException {
        ++p;
        checkScope();
        return parent.read();
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