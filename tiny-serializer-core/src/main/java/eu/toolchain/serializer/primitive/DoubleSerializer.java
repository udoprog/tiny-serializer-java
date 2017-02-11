package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;

public class DoubleSerializer implements Serializer<Double> {
  @Override
  public void serialize(SerialWriter buffer, Double value) throws IOException {
    final byte[] bytes = new byte[Double.BYTES];
    LongSerializer.toBytes(Double.doubleToLongBits(value), bytes, 0);
    buffer.write(bytes);
  }

  @Override
  public Double deserialize(SerialReader buffer) throws IOException {
    final byte[] b = new byte[Double.BYTES];
    buffer.read(b);
    return Double.longBitsToDouble(LongSerializer.fromBytes(b, 0));
  }

  @Override
  public int size() {
    return Double.BYTES;
  }
}
