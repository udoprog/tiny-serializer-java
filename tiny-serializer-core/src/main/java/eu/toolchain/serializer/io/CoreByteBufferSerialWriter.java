package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class CoreByteBufferSerialWriter extends AbstractSerialWriter {
  private final ByteBuffer buffer;

  long position = 0L;

  public CoreByteBufferSerialWriter(final ByteBuffer buffer) {
    super();
    this.buffer = buffer;
  }

  public CoreByteBufferSerialWriter(
    final SharedPool pool, final Serializer<Integer> scopeSize, final ByteBuffer buffer
  ) {
    super(pool, scopeSize);
    this.buffer = buffer;
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public void write(final ByteBuffer buffer) throws IOException {
    final int length = buffer.remaining();

    try {
      this.buffer.put(buffer);
    } catch (final BufferOverflowException e) {
      throw new EOFException();
    }

    position += length;
  }

  @Override
  public void write(byte b) throws IOException {
    try {
      buffer.put(b);
    } catch (final BufferOverflowException e) {
      throw new EOFException();
    }

    position += 1;
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    try {
      buffer.put(bytes, offset, length);
    } catch (final BufferOverflowException e) {
      throw new EOFException();
    }

    position += length;
  }

  @Override
  public void close() throws IOException {
    super.close();
    buffer.flip();
  }
}
