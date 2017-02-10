package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialReader;

public class CoreByteArraySerialReaderTest extends AbstractSerialReaderTest {
  @Override
  protected SerialReader setupSerialReader(final byte[] bytes) {
    return new CoreByteArraySerialReader(bytes);
  }
}
