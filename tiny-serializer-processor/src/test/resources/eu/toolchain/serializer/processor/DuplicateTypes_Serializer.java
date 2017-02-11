package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class DuplicateTypes_Serializer implements Serializer<DuplicateTypes> {
  final Serializer<String> s_String;
  final Serializer<String> s_common;

  public DuplicateTypes_Serializer(
    final SerializerFramework framework, final Serializer<String> p_common
  ) {
    this.s_String = framework.string();
    this.s_common = p_common;
  }

  @Override
  public void serialize(final SerialWriter buffer, final DuplicateTypes value) throws IOException {
    s_String.serialize(buffer, value.getA());
    s_String.serialize(buffer, value.getB());
    s_common.serialize(buffer, value.getC());
    s_common.serialize(buffer, value.getD());
  }

  @Override
  public DuplicateTypes deserialize(final SerialReader buffer) throws IOException {
    final String v_a = s_String.deserialize(buffer);
    final String v_b = s_String.deserialize(buffer);
    final String v_c = s_common.deserialize(buffer);
    final String v_d = s_common.deserialize(buffer);
    return new DuplicateTypes(v_a, v_b, v_c, v_d);
  }
}
