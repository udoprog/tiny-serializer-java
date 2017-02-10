package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Getter_Serializer implements Serializer<Getter> {
  final Serializer<String> s_String;

  public Getter_Serializer(final SerializerFramework framework) {
    this.s_String = framework.string();
  }

  @Override
  public void serialize(final SerialWriter buffer, final Getter value) throws IOException {
    s_String.serialize(buffer, value.getString());
  }

  @Override
  public Getter deserialize(final SerialReader buffer) throws IOException {
    final String v_string = s_String.deserialize(buffer);
    return new Getter(v_string);
  }
}
