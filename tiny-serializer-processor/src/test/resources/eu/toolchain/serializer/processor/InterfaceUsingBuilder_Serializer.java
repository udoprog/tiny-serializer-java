package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class InterfaceUsingBuilder_Serializer implements Serializer<InterfaceUsingBuilder> {
    final Serializer<String> s_String;

    public InterfaceUsingBuilder_Serializer(final SerializerFramework framework) {
        s_String = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final InterfaceUsingBuilder value) throws IOException {
        s_String.serialize(buffer, value.name());
    }

    @Override
    public InterfaceUsingBuilder deserialize(final SerialReader buffer) throws IOException {
        final String v_name = s_String.deserialize(buffer);
        return new InterfaceUsingBuilder.Builder().name(v_name).build();
    }
}