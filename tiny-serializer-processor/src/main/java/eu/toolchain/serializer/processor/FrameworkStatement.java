package eu.toolchain.serializer.processor;

@FunctionalInterface
public interface FrameworkStatement {
  Instance build(Object framework);

  default boolean isCustom() {
    return false;
  }

  @FunctionalInterface
  interface Instance {
    void writeTo(FrameworkMethodBuilder builder);
  }
}
