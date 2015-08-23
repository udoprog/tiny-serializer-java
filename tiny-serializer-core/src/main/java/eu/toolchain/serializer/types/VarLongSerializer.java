package eu.toolchain.serializer.types;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

/**
 * Based on the same concept as {@link VarIntSerializer} expanded to 64 bits.
 *
 * @see VarIntSerializer
 * @author udoprog
 */
public class VarLongSerializer implements Serializer<Long> {
    private static final int CONT = 0x80;
    private static final int MASK = (CONT ^ 0xff);

    @Override
    public void serialize(SerialWriter buffer, Long value) throws IOException {
        long v = value;

        while ((v >>> 7) > 0) {
            buffer.write(((int) (v & MASK)) | CONT);
            v = (v >>> 7);
        }

        buffer.write((int) v);
    }

    @Override
    public Long deserialize(SerialReader buffer) throws IOException {
        long v = 0;
        long shift = 1;

        int position = 0;

        while (position++ < 10) {
            byte b = buffer.read();

            v += (b & MASK) * shift;

            if ((b & CONT) == 0) {
                return (long) v;
            }

            shift <<= 7;
        }

        throw new IOException("Too many continuation bytes");
    }
}