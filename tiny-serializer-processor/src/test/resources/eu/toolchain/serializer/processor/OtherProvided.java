package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class OtherProvided {
  final String string;
  final Provided provided;
  final ValueProvided valueProvided;

  public OtherProvided(String string, Provided provided, ValueProvided valueProvided) {
    this.string = string;
    this.provided = provided;
    this.valueProvided = valueProvided;
  }

  public String getString() {
    return string;
  }

  public Provided getProvided() {
    return provided;
  }

  public ValueProvided getValueProvided() {
    return valueProvided;
  }
}
