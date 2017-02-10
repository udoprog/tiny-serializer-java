package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialReader;
import java.io.ByteArrayInputStream;

public class CoreInputStreamSerialReaderTest extends AbstractSerialReaderTest {
  @Override
  protected SerialReader setupSerialReader(final byte[] bytes) throws Exception {
    return new CoreInputStreamSerialReader(new ByteArrayInputStream(bytes));
  }
}
