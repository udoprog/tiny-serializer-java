package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class External {
  final String visible;
  @AutoSerialize.Field(external = true)
  final String hidden;
  final String visibleAgain;

  public External(final String visible, final String hidden, final String visibleAgain) {
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
