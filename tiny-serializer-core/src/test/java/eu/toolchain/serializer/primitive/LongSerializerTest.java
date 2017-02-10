package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.Helpers;
import java.io.IOException;
import org.junit.Test;

public class LongSerializerTest {
  private void roundtrip(long value, int... pattern) throws IOException {
    Helpers.roundTripPattern(new LongSerializer(), value, pattern);
  }

  @Test
  public void testValidValues() throws IOException {
    roundtrip(0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
    roundtrip(-1, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
    roundtrip(0xffffffffl, 0x00, 0x00, 0x00, 0x00, 0xff, 0xff, 0xff, 0xff);
    roundtrip(-0x100000000l, 0xff, 0xff, 0xff, 0xff, 0x00, 0x00, 0x00, 0x00);
    roundtrip(Long.MIN_VALUE, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
    roundtrip(Long.MAX_VALUE, 0x7f, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff);
  }
}
