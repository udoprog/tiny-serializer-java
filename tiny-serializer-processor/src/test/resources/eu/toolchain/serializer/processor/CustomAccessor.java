package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class CustomAccessor {
  @AutoSerialize.Field(accessor = "foo")
  final String string;

  public CustomAccessor(String string) {
    this.string = string;
  }

  public String foo() {
    return string;
  }
}
