package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class IgnoreFinalAssigned_Serializer implements Serializer<IgnoreFinalAssigned> {
    public IgnoreFinalAssigned_Serializer(final SerializerFramework framework) {
    }

    @Override
    public void serialize(final SerialWriter buffer, final IgnoreFinalAssigned value) throws IOException {
    }

    @Override
    public IgnoreFinalAssigned deserialize(final SerialReader buffer) throws IOException {
        return new IgnoreFinalAssigned();
    }
}