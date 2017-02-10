package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class DuplicateTypes {
  final String a;
  final String b;

  @AutoSerialize.Field(provided = true)
  final String c;

  @AutoSerialize.Field(provided = true)
  final String d;

  public DuplicateTypes(String a, String b, String c, String d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  public String getA() {
    return a;
  }

  public String getB() {
    return b;
  }

  public String getC() {
    return c;
  }

  public String getD() {
    return d;
  }
}
