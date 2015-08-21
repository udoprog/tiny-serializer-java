package eu.toolchain.serializer.var;

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
    private static final int CONT = 0x80;
    private static final int MASK = (CONT ^ 0xff);

    @Override
    public void serialize(SerialWriter buffer, Integer value) throws IOException {
        int v = value;

        while ((v >>> 7) > 0) {
            buffer.write((v & MASK) | CONT);
            v = (v >>> 7);
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
        }

        throw new IOException("Too many continuation bytes");
    }
}