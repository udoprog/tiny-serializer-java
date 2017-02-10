package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Ignore_Serializer implements Serializer<Ignore> {
  final Serializer<String> s_String;

  public Ignore_Serializer(final SerializerFramework framework) {
    s_String = framework.string();
  }

  @Override
  public void serialize(final SerialWriter buffer, final Ignore value) throws IOException {
    s_String.serialize(buffer, value.getVisible());
  }

  @Override
  public Ignore deserialize(final SerialReader buffer) throws IOException {
    final String v_visible = s_String.deserialize(buffer);
    return new Ignore(v_visible);
  }
}
