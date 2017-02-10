package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
@AutoSerialize.Builder(useConstructor = true, useSetter = true)
public class UseBuilderConstructor {
  final String string;

  public UseBuilderConstructor(String string) {
    this.string = string;
  }

  public String getString() {
    return string;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String string = null;

    public Builder setString(final String string) {
      this.string = string;
      return this;
    }

    public UseBuilderConstructor build() {
      return new UseBuilderConstructor(string);
    }
  }
}
