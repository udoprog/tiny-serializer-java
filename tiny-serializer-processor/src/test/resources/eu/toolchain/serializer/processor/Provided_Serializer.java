package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class Provided_Serializer implements Serializer<Provided> {
    final Serializer<String> s0;
    final Serializer<String> s1;

    public Provided_Serializer(final SerializerFramework framework, final Serializer<String> p0) {
        s0 = p0;
        s1 = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Provided value) throws IOException {
        s0.serialize(buffer, value.getString());
        s1.serialize(buffer, value.getOther());
    }

    @Override
    public Provided deserialize(final SerialReader buffer) throws IOException {
        final String v0 = s0.deserialize(buffer);
        final String v1 = s1.deserialize(buffer);
        return new Provided(v0, v1);
    }
}