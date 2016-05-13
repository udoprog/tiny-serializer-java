package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Nested_Foo_Serializer implements Serializer<Nested.Foo> {
    final Serializer<String> s_String;

    public Nested_Foo_Serializer(final SerializerFramework framework) {
        s_String = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Nested.Foo value) throws IOException {
        s_String.serialize(buffer, value.getString());
    }

    @Override
    public Nested.Foo deserialize(final SerialReader buffer) throws IOException {
        final String v_string = s_String.deserialize(buffer);
        return new Nested.Foo(v_string);
    }
}
