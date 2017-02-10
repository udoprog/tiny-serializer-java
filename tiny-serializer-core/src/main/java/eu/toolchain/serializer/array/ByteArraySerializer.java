package eu.toolchain.serializer.array;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ByteArraySerializer implements Serializer<byte[]> {
  private final Serializer<Integer> size;

  @Override
  public void serialize(SerialWriter buffer, byte[] value) throws IOException {
    size.serialize(buffer, value.length);
    buffer.write(value);
  }

  @Override
  public byte[] deserialize(SerialReader buffer) throws IOException {
    final int length = size.deserialize(buffer);
    final byte[] value = new byte[length];
    buffer.read(value);
    return value;
  }
}
