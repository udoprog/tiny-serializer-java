package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Based on the same concept as {@link CompactVarIntSerializer} expanded to 64 bits.
 *
 * @author udoprog
 * @see CompactVarIntSerializer
 */
public class CompactVarLongSerializer implements Serializer<Long> {
  public static final int MAX_SIZE = 10;

  private static final int CONT = 0x80;
  private static final int MASK = (CONT ^ 0xff);

  @Override
  public void serialize(SerialWriter buffer, Long value) throws IOException {
    final SharedPool pool = buffer.pool();

    final ByteBuffer bytes = pool.allocate(MAX_SIZE);

    try {
      long v = value;

      long temp;

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
  public Long deserialize(SerialReader buffer) throws IOException {
    long v = 0;
    long shift = 1;

    int position = 0;

    while (position++ < MAX_SIZE) {
      final byte b = buffer.read();

      v += (b & MASK) * shift;

      if ((b & CONT) == 0) {
        return (long) v;
      }

      shift <<= 7;
      v += shift;
    }

    throw new IOException("Too many continuation bytes");
  }
}
