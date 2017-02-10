package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Ordering_Serializer implements Serializer<Ordering> {
  final Serializer<String> s_String;

  public Ordering_Serializer(final SerializerFramework framework) {
    this.s_String = framework.string();
  }

  @Override
  public void serialize(final SerialWriter buffer, final Ordering value) throws IOException {
    s_String.serialize(buffer, value.getC());
    s_String.serialize(buffer, value.getB());
    s_String.serialize(buffer, value.getA());
  }

  @Override
  public Ordering deserialize(final SerialReader buffer) throws IOException {
    final String v_c = s_String.deserialize(buffer);
    final String v_b = s_String.deserialize(buffer);
    final String v_a = s_String.deserialize(buffer);
    return new Ordering(v_b, v_c, v_a);
  }
}
