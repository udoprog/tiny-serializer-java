package eu.toolchain.serializer.array;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ByteBufferSerializer implements Serializer<ByteBuffer> {
  private final Serializer<Integer> size;

  @Override
  public void serialize(SerialWriter buffer, ByteBuffer value) throws IOException {
    // NOTE: doesn't duplicate the underlying buffer, only its state.
    final ByteBuffer readOnly = value.duplicate();
    size.serialize(buffer, readOnly.remaining());
    buffer.write(readOnly);
  }

  @Override
  public ByteBuffer deserialize(SerialReader buffer) throws IOException {
    final int length = size.deserialize(buffer);
    final ByteBuffer value = ByteBuffer.allocate(length);
    buffer.read(value);
    value.flip();
    return value;
  }
}
