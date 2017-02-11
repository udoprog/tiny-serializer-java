package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class External_Serializer implements Serializer<External> {
  final Serializer<String> s_String;
  final String v_hidden;

  public External_Serializer(final SerializerFramework framework, final String v_hidden) {
    this.s_String = framework.string();
    this.v_hidden = v_hidden;
  }

  @Override
  public void serialize(final SerialWriter buffer, final External value) throws IOException {
    s_String.serialize(buffer, value.getVisible());
    s_String.serialize(buffer, value.getVisibleAgain());
  }

  @Override
  public External deserialize(final SerialReader buffer) throws IOException {
    final String v_visible = s_String.deserialize(buffer);
    final String v_visibleAgain = s_String.deserialize(buffer);
    return new External(v_visible, v_hidden, v_visibleAgain);
  }
}
