package eu.toolchain.serializer.io;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SharedPool;
import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class CoreByteBufferSerialReader extends AbstractSerialReader {
  private final ByteBuffer buffer;

  private long position = 0L;

  public CoreByteBufferSerialReader(
    final ByteBuffer buffer
  ) {
    super();
    this.buffer = buffer.asReadOnlyBuffer();
  }

  public CoreByteBufferSerialReader(
    final SharedPool pool, final Serializer<Integer> scopeSize, final ByteBuffer buffer
  ) {
    super(pool, scopeSize);
    this.buffer = buffer.asReadOnlyBuffer();
  }

  @Override
  public long position() {
    return position;
  }

  @Override
  public byte read() throws IOException {
    try {
      final byte b = buffer.get();
      position += 1;
      return b;
    } catch (final BufferUnderflowException e) {
      throw new EOFException();
    }
  }

  @Override
  public void read(byte[] bytes, int offset, int length) throws IOException {
    try {
      buffer.get(bytes, offset, length);
    } catch (final BufferUnderflowException e) {
      throw new EOFException();
    }

    position += length;
  }

  @Override
  public void skip(int length) throws IOException {
    try {
      buffer.position(buffer.position() + length);
    } catch (final IllegalArgumentException e) {
      throw new EOFException();
    }

    position += length;
  }
}
