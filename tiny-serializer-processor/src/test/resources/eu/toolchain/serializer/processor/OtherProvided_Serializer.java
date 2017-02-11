package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class OtherProvided_Serializer implements Serializer<OtherProvided> {
  final Serializer<String> s_String;
  final Serializer<Provided> s_Provided;
  final Serializer<ValueProvided> s_ValueProvided;

  public OtherProvided_Serializer(
    final SerializerFramework framework, final Serializer<String> p_String,
    final Serializer<String> p_otherProvided, final String v_hidden
  ) {
    this.s_String = framework.string();
    this.s_Provided = new Provided_Serializer(framework, p_String, p_otherProvided);
    this.s_ValueProvided = new ValueProvided_Serializer(framework, v_hidden);
  }

  @Override
  public void serialize(final SerialWriter buffer, final OtherProvided value) throws IOException {
    s_String.serialize(buffer, value.getString());
    s_Provided.serialize(buffer, value.getProvided());
    s_ValueProvided.serialize(buffer, value.getValueProvided());
  }

  @Override
  public OtherProvided deserialize(final SerialReader buffer) throws IOException {
    final String v_string = s_String.deserialize(buffer);
    final Provided v_provided = s_Provided.deserialize(buffer);
    final ValueProvided v_valueProvided = s_ValueProvided.deserialize(buffer);
    return new OtherProvided(v_string, v_provided, v_valueProvided);
  }
}
