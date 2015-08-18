package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class ImplA_Serializer implements Serializer<ImplA> {
    public ImplA_Serializer(final SerializerFramework framework) {
    }

    @Override
    public void serialize(final SerialWriter buffer, final ImplA value) throws IOException {
    }

    @Override
    public ImplA deserialize(final SerialReader buffer) throws IOException {
        return new ImplA();
    }
}