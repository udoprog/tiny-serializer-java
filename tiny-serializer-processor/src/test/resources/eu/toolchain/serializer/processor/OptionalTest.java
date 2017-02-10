package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;
import java.util.Optional;

@AutoSerialize
public class OptionalTest {
  final Optional<Interface> optional;

  public OptionalTest(Optional<Interface> optional) {
    this.optional = optional;
  }

  public Optional<Interface> getOptional() {
    return optional;
  }
}
