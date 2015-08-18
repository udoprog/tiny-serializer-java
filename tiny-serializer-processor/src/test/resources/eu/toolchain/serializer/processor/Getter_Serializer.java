package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class Getter_Serializer implements Serializer<Getter> {
    final Serializer<String> s0;

    public Getter_Serializer(final SerializerFramework framework) {
        s0 = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Getter value) throws IOException {
        s0.serialize(buffer, value.getString());
    }

    @Override
    public Getter deserialize(final SerialReader buffer) throws IOException {
        final String v0 = s0.deserialize(buffer);
        return new Getter(v0);
    }
}