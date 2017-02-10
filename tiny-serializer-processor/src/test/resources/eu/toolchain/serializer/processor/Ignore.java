package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Ignore {
  final String visible;

  @AutoSerialize.Ignore
  final String hidden;

  public Ignore(final String visible) {
    this.visible = visible;
    this.hidden = visible.toUpperCase();
  }

  public String getVisible() {
    return visible;
  }
}
