package eu.toolchain.serializer.array;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

/**
 * A boolean array serializer that encodes boolean into bytes, through bit offsets.
 *
 * @author udoprog
 */
@RequiredArgsConstructor
public class BooleanArraySerializer implements Serializer<boolean[]> {
  /* size in bits of a single byte */
  public static final int SIZE = 8;

  private final Serializer<Integer> size;

  @Override
  public void serialize(SerialWriter buffer, boolean[] value) throws IOException {
    final int size = (value.length + (SIZE - 1)) / SIZE;
    final byte[] bytes = new byte[size];

    for (int i = 0; i < value.length; i++) {
      if (value[i]) {
        bytes[i / SIZE] |= (1 << (i % SIZE));
      }
    }

    this.size.serialize(buffer, value.length);
    buffer.write(bytes);
  }

  @Override
  public boolean[] deserialize(SerialReader buffer) throws IOException {
    final int length = size.deserialize(buffer);

    final byte[] bytes = new byte[(length + (SIZE - 1)) / SIZE];
    final boolean[] value = new boolean[length];

    buffer.read(bytes);

    for (int i = 0; i < value.length; i++) {
      value[i] = (bytes[i / SIZE] & (1 << (i % SIZE))) != 0;
    }

    return value;
  }
}
