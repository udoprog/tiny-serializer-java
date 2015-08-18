package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;

public final class Empty_Serializer implements Serializer<Empty> {
    public Empty_Serializer(final SerializerFramework framework) {
    }

    @Override
    public void serialize(final SerialWriter buffer, final Empty value) throws IOException {
    }

    @Override
    public Empty deserialize(final SerialReader buffer) throws IOException {
        return new Empty();
    }
}