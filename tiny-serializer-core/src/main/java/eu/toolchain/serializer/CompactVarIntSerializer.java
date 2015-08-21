package eu.toolchain.serializer;

import java.io.IOException;

public class CompactVarIntSerializer implements Serializer<Integer> {
    private static final int CONT = 0x80;
    private static final int MASK = (CONT ^ 0xff);

    @Override
    public void serialize(SerialWriter buffer, Integer value) throws IOException {
        int v = value;

        while ((v >>> 7) > 0) {
            buffer.write((v & MASK) | CONT);
            v = (v >>> 7) - 1;
        }

        buffer.write(v);
        return;
    }

    @Override
    public Integer deserialize(SerialReader buffer) throws IOException {
        int v = 0;
        long shift = 1;

        int position = 0;

        while (position++ < 5) {
            byte b = buffer.read();

            v += (b & MASK) * shift;

            if ((b & CONT) == 0) {
                return v;
            }

            shift <<= 7;
            v += shift;
        }

        throw new IOException("Too many continuation bytes");
    }
}