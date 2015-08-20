package eu.toolchain.serializer.processor;

import java.util.Optional;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class OptionalTest {
    final Optional<Interface> optional;

    public OptionalTest(Optional<Interface> optional) {
        this.optional = optional;
    }

    public Optional<Interface> getOptional() {
        return optional;
    }
}