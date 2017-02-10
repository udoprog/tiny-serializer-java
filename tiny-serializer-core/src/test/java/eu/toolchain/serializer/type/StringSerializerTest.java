package eu.toolchain.serializer.type;

import eu.toolchain.serializer.Helpers;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.primitive.CompactVarIntSerializer;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class StringSerializerTest {
  private Serializer<String> string;

  @Before
  public void setup() {
    string = new StringSerializer(new CompactVarIntSerializer());
  }

  @Test
  public void basicTests() throws IOException {
    Helpers.roundtrip(string, "Hello World");
    Helpers.roundtrip(string, "Hello: åäö");
  }
}
