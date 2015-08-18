package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class MissingEmptyConstructor {
    final String string;

    public MissingEmptyConstructor(String string) {
        this.string = string;
    }
}