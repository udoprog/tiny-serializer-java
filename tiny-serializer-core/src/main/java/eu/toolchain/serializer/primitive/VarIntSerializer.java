package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Variable-length number encoding based on continuation bits.
 * <p>
 * Each byte carries 7 bits of data, the first bit indicates whether the sequence continues or not.
 * Any bit not included in the sequence is padded to 0.
 * <p>
 * <h1>Example</h1>
 * <p>
 * <pre>
 * 00000000 00000000 10001000 01010101
 *             zzzzz zzyyyyyy yxxxxxxx
 * </pre>
 * <p>
 * Is encoded as:
 * <p>
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
        final SharedPool pool = buffer.pool();

        final ByteBuffer bytes = pool.allocate(MAX_SIZE);

        try {
            int v = value;

            int temp;

            while ((temp = (v >>> 7)) > 0) {
                bytes.put((byte) ((v & MASK) | CONT));
                v = temp;
            }

            bytes.put((byte) v);
            bytes.flip();
            buffer.write(bytes);
        } finally {
            pool.release(MAX_SIZE);
        }
    }

    @Override
    public Integer deserialize(SerialReader buffer) throws IOException {
        int v = 0;
        long shift = 1;

        int position = 0;

        while (position++ < MAX_SIZE) {
            final byte b = buffer.read();

            v += (b & MASK) * shift;

            if ((b & CONT) == 0) {
                return v;
            }

            shift <<= 7;
        }

        throw new IOException("Too many continuation bytes");
    }
}
