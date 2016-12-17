package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class FieldBased_Serializer implements Serializer<FieldBased> {
    final Serializer<Integer> count;

    final Serializer<String> name;

    final Serializer<String> s_String;
    final Serializer<Boolean> s_Boolean;
    final Serializer<Optional<String>> s_OptionalString;

    public FieldBased_Serializer(final SerializerFramework framework) {
        count = framework.variableInteger();
        name = framework.string();

        s_String = framework.string();
        s_Boolean = framework.fixedBoolean();
        s_OptionalString = framework.optional(framework.string());
    }

    @Override
    public void serialize(final SerialWriter buffer, final FieldBased value) throws IOException {
        count.serialize(buffer, 3);

        name.serialize(buffer, "string");

        try (final SerialWriter w = buffer.scope()) {
            s_String.serialize(w, value.getString());
        }

        name.serialize(buffer, "hello");

        try (final SerialWriter w = buffer.scope()) {
            s_Boolean.serialize(w, value.isHello());
        }

        final Optional<String> v_optional = value.getOptional();

        if (v_optional.isPresent()) {
            name.serialize(buffer, "optional");

            try (final SerialWriter w = buffer.scope()) {
                s_OptionalString.serialize(w, v_optional);
            }
        }
    }

    @Override
    public FieldBased deserialize(final SerialReader buffer) throws IOException {
        String v_string = null;
        boolean i_string = false;
        boolean v_hello = false;
        boolean i_hello = false;
        Optional<String> v_optional = Optional.empty();

        final int total = count.deserialize(buffer);

        int i = 0;

        while (i++ < total) {
            final String fieldName = name.deserialize(buffer);

            switch (fieldName) {
            case "string":
                try (final SerialReader r = buffer.scope()) {
                    v_string = s_String.deserialize(r);
                }

                i_string = true;
                break;
            case "hello":
                try (final SerialReader r = buffer.scope()) {
                    v_hello = s_Boolean.deserialize(r);
                }

                i_hello = true;
                break;
            case "optional":
                try (final SerialReader r = buffer.scope()) {
                    v_optional = s_OptionalString.deserialize(r);
                }

                break;
            default:
                buffer.skip();
                break
            }
        }

        if (!i_string) {
            throw new IOException("Missing required field: string");
        }

        if (!i_hello) {
            throw new IOException("Missing required field: hello");
        }

        return new FieldBased(v_string, v_hello, v_optional);
    }
}
