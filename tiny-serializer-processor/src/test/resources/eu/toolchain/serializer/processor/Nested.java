package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

public class Nested {
    @AutoSerialize
    public static class Foo {
        final String string;

        public Foo(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }
}