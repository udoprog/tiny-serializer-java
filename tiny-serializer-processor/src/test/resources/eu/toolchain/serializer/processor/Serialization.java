package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;

public final class Serialization {
    private final SerializerFramework framework;

    public Serialization(final SerializerFramework f) {
        framework = f;
    }

    public Serializer<Empty> getEmpty() {
        return new Empty_Serializer(framework);
    }

    public Serializer<Interface> getInterface() {
        return new Interface_Serializer(framework);
    }

    public Serializer<ImplA> getImplA() {
        return new ImplA_Serializer(framework);
    }

    public Serializer<ImplB> getImplB() {
        return new ImplB_Serializer(framework);
    }
}