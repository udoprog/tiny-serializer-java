package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class ImplicitConstructor {
  final String string;

  public ImplicitConstructor(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }
}
