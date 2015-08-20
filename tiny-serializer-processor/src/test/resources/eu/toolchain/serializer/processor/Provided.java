package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Provided {
    @AutoSerialize.Field(provided = true)
    final String string;

    @AutoSerialize.Field(provided = true, providerName="otherProvided")
    final String otherProvided;

    final String other;

    public Provided(String string, String otherProvided, String other) {
        this.string = string;
        this.otherProvided = otherProvided;
        this.other = other;
    }

    public String getString() {
        return string;
    }

    public String getOtherProvided() {
        return otherProvided;
    }

    public String getOther() {
        return other;
    }
}