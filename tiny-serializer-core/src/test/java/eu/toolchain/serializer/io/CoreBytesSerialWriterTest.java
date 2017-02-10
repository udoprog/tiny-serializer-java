package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialWriter;

public class CoreBytesSerialWriterTest extends AbstractSerialWriterTest {
  @Override
  protected SerialWriter setupSerialWriter() {
    return new CoreBytesSerialWriter();
  }
}
