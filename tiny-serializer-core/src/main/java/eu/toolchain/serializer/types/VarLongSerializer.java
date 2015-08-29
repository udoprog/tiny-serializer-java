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
    public static final int MAX_SIZE = 10;

    private static final int CONT = 0x80;
    private static final int MASK = (CONT ^ 0xff);

    @Override
    public void serialize(SerialWriter buffer, Long value) throws IOException {
        final byte[] bytes = new byte[MAX_SIZE];

        int i = 0;
        long v = value;

        long temp;

        while ((temp = (v >>> 7)) > 0) {
            bytes[i++] = (byte)((v & MASK) | CONT);
            v = temp;
        }

        bytes[i++] = (byte)v;
        buffer.write(bytes, 0, i);
    }

    @Override
    public Long deserialize(SerialReader buffer) throws IOException {
        final byte[] bytes = new byte[1];

        long v = 0;
        long shift = 1;

        int position = 0;

        while (position++ < MAX_SIZE) {
            buffer.read(bytes);

            byte b = bytes[0];

            v += (b & MASK) * shift;

            if ((b & CONT) == 0) {
                return (long) v;
            }

            shift <<= 7;
        }

        throw new IOException("Too many continuation bytes");
    }
}