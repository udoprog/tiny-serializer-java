package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize(useGetter = true)
public class Getter {
    private final String string;

    @AutoSerialize.Creator
    public Getter(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}