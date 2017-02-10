package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class OptionalTest_Serializer implements Serializer<OptionalTest> {
  final Serializer<Optional<Interface>> s_OptionalInterface;

  public OptionalTest_Serializer(final SerializerFramework framework) {
    s_OptionalInterface = framework.optional(new Interface_Serializer(framework));
  }

  @Override
  public void serialize(final SerialWriter buffer, final OptionalTest value) throws IOException {
    s_OptionalInterface.serialize(buffer, value.getOptional());
  }

  @Override
  public OptionalTest deserialize(final SerialReader buffer) throws IOException {
    final Optional<Interface> v_optional = s_OptionalInterface.deserialize(buffer);
    return new OptionalTest(v_optional);
  }
}
