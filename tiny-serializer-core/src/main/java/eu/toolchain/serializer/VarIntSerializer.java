package eu.toolchain.serializer;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class VarIntSerializer implements Serializer<Integer> {
    @Override
    public void serialize(SerialWriter buffer, Integer value) throws IOException {
        int v = value;

        if (v < 0)
            throw new IllegalArgumentException("negative integers are not supported");

        if (v <= 0x7f) {
            buffer.write((byte) (v & 0x7f));
            return;
        }

        if (v <= 0x3fff) {
            buffer.write(v & 0x7f | 0x80);
            buffer.write(v >>> 7);
            return;
        }

        if (v <= 0x1fffff) {
            buffer.write(v | 0x80);
            buffer.write(v >>> 7 | 0x80);
            buffer.write(v >>> 14);
            return;
        }

        if (v <= 0xfffffff) {
            buffer.write(v | 0x80);
            buffer.write(v >>> 7 | 0x80);
            buffer.write(v >>> 14 | 0x80);
            buffer.write(v >>> 21);
            return;
        }

        buffer.write(v | 0x80);
        buffer.write(v >>> 7 | 0x80);
        buffer.write(v >>> 14 | 0x80);
        buffer.write(v >>> 21 | 0x80);
        buffer.write(v >>> 28);
        return;
    }

    @Override
    public Integer deserialize(SerialReader buffer) throws IOException {
        long v = 0;

        final byte a = buffer.read();

        v |= a & 0x7f;

        if ((a & 0x80) == 0)
            return (int) v;

        final byte b = buffer.read();

        v |= ((b & 0x7f) << 7);

        if ((b & 0x80) == 0)
            return (int) v;

        final byte c = buffer.read();

        v |= ((c & 0x7f) << 14);

        if ((c & 0x80) == 0)
            return (int) v;

        final byte d = buffer.read();

        v |= ((d & 0x7f) << 21);

        if ((d & 0x80) == 0)
            return (int) v;

        final byte e = buffer.read();
        return (int) (v | ((e & 0x7f) << 28));
    }
}