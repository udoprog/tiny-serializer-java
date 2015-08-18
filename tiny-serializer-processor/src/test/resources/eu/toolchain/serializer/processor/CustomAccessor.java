package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class CustomAccessor {
    final String string;

    @AutoSerialize.Creator
    public CustomAccessor(@AutoSerialize.Field(accessor = "foo") String string) {
        this.string = string;
    }

    public String foo() {
        return string;
    }
}