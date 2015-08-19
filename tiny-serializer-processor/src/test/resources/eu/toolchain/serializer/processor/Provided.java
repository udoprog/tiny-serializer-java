package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Provided {
    @AutoSerialize.Field(provided = true)
    final String string;

    final String other;

    public Provided(String string, String other) {
        this.string = string;
        this.other = other;
    }

    public String getString() {
        return string;
    }

    public String getOther() {
        return other;
    }
}