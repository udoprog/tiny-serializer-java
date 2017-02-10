package eu.toolchain.serializer.primitive;

import eu.toolchain.serializer.Helpers;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import org.junit.Test;

public class VarIntSerializerTest {
  final Serializer<Integer> regular = new VarIntSerializer();
  final Serializer<Integer> compact = new CompactVarIntSerializer();

  @Test
  public void testEdgeValues() throws IOException {
    Helpers.roundtrip(regular, Integer.MIN_VALUE);
    Helpers.roundtrip(regular, Integer.MIN_VALUE);

    int v = 1;

    for (int i = 0; i < 6; i++) {
      Helpers.roundtrip(regular, v - 1);
      Helpers.roundtrip(regular, v);
      v = (v << 7);
    }
  }

  @Test
  public void testCompactEdgeValues() throws IOException {
    Helpers.roundtrip(compact, Integer.MIN_VALUE);
    Helpers.roundtrip(compact, Integer.MIN_VALUE);

    int v = 0;

    for (int i = 0; i < 6; i++) {
      Helpers.roundtrip(compact, v - 1);
      Helpers.roundtrip(compact, v);
      v = ((v + 1) << 7);
    }
  }
}
