package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Interface_Serializer implements Serializer<Interface> {
    final Serializer<Interface> serializer;

    public Interface_Serializer(final SerializerFramework framework) {
        final List<SerializerFramework.TypeMapping<? extends Interface, Interface>> mappings = new ArrayList<>();

        mappings.add(framework.<ImplA, Interface> type(0, ImplA.class, new ImplA_Serializer(framework)));
        mappings.add(framework.<ImplB, Interface> type(1, ImplB.class, new ImplB_Serializer(framework)));

        serializer = framework.subtypes(mappings);
    }

    @Override
    public void serialize(final SerialWriter buffer, final Interface value) throws IOException {
        serializer.serialize(buffer, value);
    }

    @Override
    public Interface deserialize(final SerialReader buffer) throws IOException {
        return serializer.deserialize(buffer);
    }
}