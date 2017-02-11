package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class OtherExternal {
  final String string;
  final Provided provided;
  @AutoSerialize.Field(external = true)
  final String hidden;
  final External external;

  public OtherExternal(String string, Provided provided, String hidden, External external) {
    this.string = string;
    this.provided = provided;
    this.hidden = hidden;
    this.external = external;
  }

  public String getString() {
    return string;
  }

  public Provided getProvided() {
    return provided;
  }

  public External getExternal() {
    return external;
  }
}
