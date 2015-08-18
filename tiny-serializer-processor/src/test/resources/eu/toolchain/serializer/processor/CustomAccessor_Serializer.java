package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class CustomAccessor_Serializer implements Serializer<CustomAccessor> {
    final Serializer<String> s0;

    public CustomAccessor_Serializer(final SerializerFramework framework) {
        s0 = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final CustomAccessor value) throws IOException {
        s0.serialize(buffer, value.foo());
    }

    @Override
    public CustomAccessor deserialize(final SerialReader buffer) throws IOException {
        final String v0 = s0.deserialize(buffer);
        return new CustomAccessor(v0);
    }
}