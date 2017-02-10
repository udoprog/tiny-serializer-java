package eu.toolchain.examples;

import eu.toolchain.serializer.TinySerializer;

public class SerializerSetup {
  public static TinySerializer.Builder setup() {
    final TinySerializer.Builder builder = TinySerializer.builder();

        /* Configure a custom Serializer for collection sizes. */
    builder.useCompactSize(true);

    return builder;
  }
}
