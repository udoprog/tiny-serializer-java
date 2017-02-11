package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class OtherExternal_Serializer implements Serializer<OtherExternal> {
  final Serializer<String> s_String;
  final Serializer<Provided> s_Provided;
  final String v_hidden;
  final Serializer<External> s_External;

  public OtherExternal_Serializer(
    final SerializerFramework framework, final Serializer<String> p_String,
    final Serializer<String> p_otherProvided, final String v_hidden, final String p_hidden
  ) {
    this.s_String = framework.string();
    this.s_Provided = new Provided_Serializer(framework, p_String, p_otherProvided);
    this.v_hidden = v_hidden;
    this.s_External = new External_Serializer(framework, p_hidden);
  }

  @Override
  public void serialize(final SerialWriter buffer, final OtherExternal value) throws IOException {
    s_String.serialize(buffer, value.getString());
    s_Provided.serialize(buffer, value.getProvided());
    s_External.serialize(buffer, value.getExternal());
  }

  @Override
  public OtherExternal deserialize(final SerialReader buffer) throws IOException {
    final String v_string = s_String.deserialize(buffer);
    final Provided v_provided = s_Provided.deserialize(buffer);
    final External v_external = s_External.deserialize(buffer);
    return new OtherExternal(v_string, v_provided, v_hidden, v_external);
  }
}
