package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;

public class FloatSerializer implements Serializer<Float> {
  @Override
  public void serialize(SerialWriter buffer, Float value) throws IOException {
    final byte[] bytes = new byte[Float.BYTES];
    IntegerSerializer.toBytes(Float.floatToIntBits(value), bytes, 0);
    buffer.write(bytes);
  }

  @Override
  public Float deserialize(SerialReader buffer) throws IOException {
    final byte[] b = new byte[Float.BYTES];
    buffer.read(b);
    return Float.intBitsToFloat(IntegerSerializer.fromBytes(b, 0));
  }

  @Override
  public int size() {
    return Float.BYTES;
  }
}
