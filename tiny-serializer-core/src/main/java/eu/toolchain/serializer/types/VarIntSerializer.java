package eu.toolchain.serializer.types;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

/**
 * Variable-length number encoding based on continuation bits.
 *
 * Each byte carries 7 bits of data, the first bit indicates whether the sequence continues or not. Any bit not included
 * in the sequence is padded to 0.
 *
 * <h1>Example</h1>
 *
 * <pre>
 * 00000000 00000000 10001000 01010101
 *             zzzzz zzyyyyyy yxxxxxxx
 * </pre>
 *
 * Is encoded as:
 *
 * <pre>
 * 11010101 10010000 00000010
 * Cxxxxxxx Cyyyyyyy Czzzzzzz
 * </pre>
 *
 * @author udoprog
 */
public class VarIntSerializer implements Serializer<Integer> {
    public static final int MAX_SIZE = 5;

    private static final int CONT = 0x80;
    private static final int MASK = (CONT ^ 0xff);

    @Override
    public void serialize(SerialWriter buffer, Integer value) throws IOException {
        final byte[] bytes = new byte[MAX_SIZE];

        int i = 0;
        int v = value;

        int temp;

        while ((temp = (v >>> 7)) > 0) {
            bytes[i++] = (byte)((v & MASK) | CONT);
            v = temp;
        }

        bytes[i++] = (byte)v;
        buffer.write(bytes, 0, i);
        return;
    }

    @Override
    public Integer deserialize(SerialReader buffer) throws IOException {
        final byte[] bytes = new byte[1];

        int v = 0;
        long shift = 1;

        int position = 0;

        while (position++ < MAX_SIZE) {
            buffer.read(bytes);

            final byte b = bytes[0];

            v += (b & MASK) * shift;

            if ((b & CONT) == 0) {
                return v;
            }

            shift <<= 7;
        }

        throw new IOException("Too many continuation bytes");
    }
}