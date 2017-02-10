package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;

public class ByteSerializer implements Serializer<Byte> {
  @Override
  public void serialize(SerialWriter buffer, Byte value) throws IOException {
    buffer.write(value);
  }

  @Override
  public Byte deserialize(SerialReader buffer) throws IOException {
    return buffer.read();
  }
}
