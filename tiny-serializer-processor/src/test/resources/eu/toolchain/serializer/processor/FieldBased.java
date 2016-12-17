package eu.toolchain.serializer.processor;

import java.util.Optional;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize(fieldBased = true, failOnMissing = false)
public class FieldBased {
    private final String string;
    private final boolean hello;
    private final Optional<String> optional;

    public FieldBased(String string, boolean hello, Optional<String> optional) {
        this.string = string;
        this.hello = hello;
        this.optional = optional;
    }

    public String getString() {
        return string;
    }

    public boolean isHello() {
        return hello;
    }

    public Optional<String> getOptional() {
        return optional;
    }
}
