package eu.toolchain.serializer.io;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FakeWritableByteChannel implements WritableByteChannel {
  private final byte[] bytes;

  private boolean open = true;
  private long position = 0L;

  @Override
  public int write(final ByteBuffer dst) throws IOException {
    final int length = dst.remaining();
    final long target = position + length;

    if (target >= bytes.length) {
      throw new EOFException();
    }

    dst.get(bytes, (int) position, length);
    position += length;
    return length;
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void close() throws IOException {
    if (!open) {
      throw new ClosedChannelException();
    }

    open = false;
  }
}
