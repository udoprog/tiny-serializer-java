package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.processor.field.FieldSet;
import java.util.Optional;

@FunctionalInterface
public interface FrameworkStatement {
  Instance build(Optional<FieldSet> fields, Object framework);

  default boolean isCustom() {
    return false;
  }

  @FunctionalInterface
  interface Instance {
    void writeTo(FrameworkMethodBuilder builder);
  }
}
