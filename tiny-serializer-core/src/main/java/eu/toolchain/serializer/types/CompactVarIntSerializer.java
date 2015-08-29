package eu.toolchain.serializer.types;

import java.io.IOException;
import java.nio.ByteBuffer;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;

/**
 * Variable-length number encoding based on continuation bits.
 *
 * This is very similar to {@link VarIntSerializer}, with the exception that the encoding achieves a perfect mapping for
 * a sequence of bytes to a single number.
 *
 * Consider the following case.
 *
 * <pre>
 * 10000000 00000000
 * </pre>
 *
 * This essentially represents the same as a single byte, with no continuation of.
 *
 * <pre>
 * 00000000
 * </pre>
 *
 * We can do better by mapping each *continuation* to a specific value, subtract that value during encoding, and add it
 * back during decoding.
 *
 * So each continuation would have a value of (1 << (7 * n)) which we can subtract from the 7 bit encoded message, and
 * re-encode it.
 *
 * Using this technique, we can represent 2113665 more values with 5 bytes than was previously possible. This also means
 * that the encoding is more compact (hence the name) because these bits are coalesced towards zero.
 *
 * @author udoprog
 */
public class CompactVarIntSerializer implements Serializer<Integer> {
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
                v = temp - 1;
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
            v += shift;
        }

        throw new IOException("Too many continuation bytes");
    }
}