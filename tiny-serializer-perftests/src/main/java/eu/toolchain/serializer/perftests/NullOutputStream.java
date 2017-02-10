package eu.toolchain.serializer.perftests;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

public class NullOutputStream extends OutputStream {
  final AtomicLong bytes = new AtomicLong();

  @Override
  public void write(int b) throws IOException {
    bytes.incrementAndGet();
  }
}
