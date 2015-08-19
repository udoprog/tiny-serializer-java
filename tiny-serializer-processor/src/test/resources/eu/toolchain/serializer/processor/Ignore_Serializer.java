package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class Ignore_Serializer implements Serializer<Ignore> {
    final Serializer<String> s0;

    public Ignore_Serializer(final SerializerFramework framework) {
        s0 = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Ignore value) throws IOException {
        s0.serialize(buffer, value.getVisible());
    }

    @Override
    public Ignore deserialize(final SerialReader buffer) throws IOException {
        final String v0 = s0.deserialize(buffer);
        return new Ignore(v0);
    }
}