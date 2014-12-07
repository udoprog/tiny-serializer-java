package eu.toolchain.examples;

import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.VarIntSerializer;

public class SerializerSetup {
    public static TinySerializer setup() {
        final TinySerializer.Builder builder = TinySerializer.builder();

        /* Configure a custom Serializer for collection sizes. */
        builder.collectionSize(new VarIntSerializer());

        return builder.build();
    }
}
