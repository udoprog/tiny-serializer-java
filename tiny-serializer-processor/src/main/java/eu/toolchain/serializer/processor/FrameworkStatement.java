package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.processor.field.Field;
import java.util.List;

@FunctionalInterface
public interface FrameworkStatement {
  Instance build(List<Field> fields, Object framework);

  default boolean isCustom() {
    return false;
  }

  @FunctionalInterface
  interface Instance {
    void writeTo(FrameworkMethodBuilder builder);
  }
}
