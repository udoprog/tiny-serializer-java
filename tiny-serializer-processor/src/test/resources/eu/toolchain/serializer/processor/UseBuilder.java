package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize(useBuilder = true, useBuilderSetter = true)
public class UseBuilder {
    final String string;

    public UseBuilder(String string) {
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

        public UseBuilder build() {
            return new UseBuilder(string);
        }
    }
}