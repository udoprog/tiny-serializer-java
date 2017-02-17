package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize(useGetter = false)
@AutoSerialize.Builder(type = InterfaceUsingBuilder.Builder.class, useConstructor = true)
public interface InterfaceUsingBuilder {
  String name();

  public static class Builder {
    public Builder() {
    }

    public Builder name(final String name) {
      return this;
    }

    public InterfaceUsingBuilder build() {
      throw new RuntimeException("not implemented");
    }
  }
}
