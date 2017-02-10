package eu.toolchain.serializer.io;

import eu.toolchain.serializer.BytesSerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class CoreBytesSerialWriter extends AbstractSerialWriter implements BytesSerialWriter {
  private final ByteArrayOutputStream output = new ByteArrayOutputStream();

  long position = 0L;

  public CoreBytesSerialWriter() {
    super();
  }

  public CoreBytesSerialWriter(final SharedPool pool, final Serializer<Integer> scopeSize) {
    super(pool, scopeSize);
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public void write(byte b) {
    output.write(b & 0xff);
    position += 1;
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    output.write(bytes, offset, length);
    position += length;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public byte[] toByteArray() {
    return output.toByteArray();
  }

  @Override
  public ByteBuffer toByteBuffer() {
    return ByteBuffer.wrap(toByteArray());
  }
}
