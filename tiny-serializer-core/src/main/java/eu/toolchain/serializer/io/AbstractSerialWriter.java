package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.CompactVarIntSerializer;

public abstract class AbstractSerialWriter implements SerialWriter {
    private static final CompactVarIntSerializer varint = new CompactVarIntSerializer();

    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public SerialWriter.Scope scope() {
        return new ScopedSerialWriter(this);
    }

    private static final class ScopedSerialWriter extends AbstractSerialWriter implements SerialWriter.Scope {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        private final SerialWriter parent;

        private ScopedSerialWriter(final SerialWriter parent) {
            this.parent = parent;
        }

        @Override
        public void write(int b) throws IOException {
            output.write(b);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            output.write(bytes, offset, length);
        }

        @Override
        public void flush() throws IOException {
            output.flush();
        }

        @Override
        public void close() throws IOException {
            varint.serialize(parent, output.size());
            parent.write(output.toByteArray());
        }
    }
}
