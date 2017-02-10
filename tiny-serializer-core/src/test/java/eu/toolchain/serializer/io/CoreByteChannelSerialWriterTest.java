package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialWriter;

public class CoreByteChannelSerialWriterTest extends AbstractSerialWriterTest {
  @Override
  protected SerialWriter setupSerialWriter() {
    return new CoreByteChannelSerialWriter(new FakeWritableByteChannel(new byte[SIZE]));
  }
}
