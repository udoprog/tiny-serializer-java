package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.Helpers;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import org.junit.Test;

public class VarLongSerializerTest {
  final VarLongSerializer regular = new VarLongSerializer();
  final Serializer<Long> compact = new CompactVarLongSerializer();

  @Test
  public void testEdgeValues() throws IOException {
    Helpers.roundtrip(regular, Long.MIN_VALUE);
    Helpers.roundtrip(regular, Long.MIN_VALUE);

    long v = 1;

    for (int i = 0; i < 10; i++) {
      Helpers.roundtrip(regular, v - 1);
      Helpers.roundtrip(regular, v);
      v = (v << 7);
    }
  }

  @Test
  public void testCompactEdgeValues() throws IOException {
    Helpers.roundtrip(compact, Long.MIN_VALUE);
    Helpers.roundtrip(compact, Long.MIN_VALUE);

    long v = 0;

    for (int i = 0; i < 10; i++) {
      Helpers.roundtrip(compact, v - 1);
      Helpers.roundtrip(compact, v);
      v = ((v + 1) << 7);
    }
  }
}
