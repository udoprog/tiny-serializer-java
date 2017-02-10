package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Abstract_Serializer implements Serializer<Abstract> {
  final Serializer<Abstract> serializer;

  public Abstract_Serializer(final SerializerFramework framework) {
    final List<SerializerFramework.TypeMapping<? extends Abstract, Abstract>> mappings =
      new ArrayList<>();

    mappings.add(framework.<ImplA, Abstract>type(0, ImplA.class, new ImplA_Serializer(framework)));
    mappings.add(framework.<ImplB, Abstract>type(1, ImplB.class, new ImplB_Serializer(framework)));

    serializer = framework.subtypes(mappings);
  }

  @Override
  public void serialize(final SerialWriter buffer, final Abstract value) throws IOException {
    serializer.serialize(buffer, value);
  }

  @Override
  public Abstract deserialize(final SerialReader buffer) throws IOException {
    return serializer.deserialize(buffer);
  }
}
