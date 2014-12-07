package eu.toolchain.serializer.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.VarIntSerializer;
import eu.toolchain.serializer.SerialWriter.Scope;

public abstract class AbstractSerialWriter implements SerialWriter {
    private static final VarIntSerializer varint = new VarIntSerializer();

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
        public void write(byte[] bytes) throws IOException {
            output.write(bytes);
        }

        @Override
        public void close() throws IOException {
            varint.serialize(parent, output.size());
            parent.write(output.toByteArray());
        }
    }
}
