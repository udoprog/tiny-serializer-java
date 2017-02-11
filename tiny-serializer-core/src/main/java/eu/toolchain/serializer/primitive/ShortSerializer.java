package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;

public class ShortSerializer implements Serializer<Short> {
  @Override
  public void serialize(SerialWriter buffer, Short value) throws IOException {
    final byte[] bytes = new byte[Short.BYTES];
    toBytes(value, bytes, 0);
    buffer.write(bytes);
  }

  @Override
  public Short deserialize(SerialReader buffer) throws IOException {
    final byte[] b = new byte[Short.BYTES];
    buffer.read(b);
    return fromBytes(b, 0);
  }

  public static void toBytes(short v, byte[] b, int o) {
    b[o] = (byte) (v >>> 8);
    b[o + 1] = (byte) v;
  }

  public static short fromBytes(final byte[] b, int o) {
    int v = 0;

    v = v + ((b[o] & 0xff) << 8);
    v = v + ((b[o + 1] & 0xff));

    return (short) v;
  }

  @Override
  public int size() {
    return Short.BYTES;
  }
}
