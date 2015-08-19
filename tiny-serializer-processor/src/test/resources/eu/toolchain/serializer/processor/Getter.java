package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Getter {
    private final String string;

    public Getter(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}