package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialWriter;
import java.io.ByteArrayOutputStream;

public class CoreOutputStreamSerialWriterTest extends AbstractSerialWriterTest {
  @Override
  protected SerialWriter setupSerialWriter() {
    return new CoreOutputStreamSerialWriter(new ByteArrayOutputStream());
  }
}
