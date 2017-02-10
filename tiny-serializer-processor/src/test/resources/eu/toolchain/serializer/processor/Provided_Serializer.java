package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Provided_Serializer implements Serializer<Provided> {
  final Serializer<String> s_ProvidedString;
  final Serializer<String> s_ProvidedString1;
  final Serializer<String> s_String;

  public Provided_Serializer(
    final SerializerFramework framework, final Serializer<String> p_String,
    final Serializer<String> p_otherProvided
  ) {
    s_ProvidedString = p_String;
    s_ProvidedString1 = p_otherProvided;
    s_String = framework.string();
  }

  @Override
  public void serialize(final SerialWriter buffer, final Provided value) throws IOException {
    s_ProvidedString.serialize(buffer, value.getString());
    s_ProvidedString1.serialize(buffer, value.getOtherProvided());
    s_String.serialize(buffer, value.getOther());
  }

  @Override
  public Provided deserialize(final SerialReader buffer) throws IOException {
    final String v_string = s_ProvidedString.deserialize(buffer);
    final String v_otherProvided = s_ProvidedString1.deserialize(buffer);
    final String v_other = s_String.deserialize(buffer);
    return new Provided(v_string, v_otherProvided, v_other);
  }
}
