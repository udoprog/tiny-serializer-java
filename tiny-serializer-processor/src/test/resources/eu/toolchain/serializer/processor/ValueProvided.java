package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class ValueProvided {
  final String visible;
  @AutoSerialize.Provided
  final String hidden;
  final String visibleAgain;

  public ValueProvided(final String visible, final String hidden, final String visibleAgain) {
    this.visible = visible;
    this.hidden = hidden;
    this.visibleAgain = visibleAgain;
  }

  public String getVisible() {
    return visible;
  }

  public String getVisibleAgain() {
    return visibleAgain;
  }
}
