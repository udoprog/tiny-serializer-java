package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;

public class CharacterSerializer implements Serializer<Character> {
  public static final int BYTES = 2;

  @Override
  public void serialize(SerialWriter buffer, Character value) throws IOException {
    final byte bytes[] = new byte[BYTES];
    toBytes(value, bytes, 0);
    buffer.write(bytes);
  }

  @Override
  public Character deserialize(SerialReader buffer) throws IOException {
    final byte[] bytes = new byte[BYTES];
    buffer.read(bytes);
    return fromBytes(bytes, 0);
  }

  public static void toBytes(char v, byte[] b, int o) {
    b[o] = (byte) (v >>> 8);
    b[o + 1] = (byte) v;
  }

  public static char fromBytes(final byte[] b, int o) {
    int v = 0;

    v = v + ((b[o] & 0xff) << 8);
    v = v + ((b[o + 1] & 0xff));

    return (char) v;
  }
}
