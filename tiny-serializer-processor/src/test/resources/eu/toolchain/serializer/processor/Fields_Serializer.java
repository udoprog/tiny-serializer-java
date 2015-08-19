package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.UUID;

public final class Fields_Serializer implements Serializer<Fields> {
    final Serializer<String> s0;
    final Serializer<Interface> s1;
    final Serializer<Short> s2;
    final Serializer<Integer> s3;
    final Serializer<Long> s4;
    final Serializer<Float> s5;
    final Serializer<Double> s6;
    final Serializer<Boolean> s7;
    final Serializer<UUID> s8;
    final Serializer<byte[]> s9;
    final Serializer<char[]> s10;

    public Fields_Serializer(final SerializerFramework framework) {
        s0 = framework.string();
        s1 = new Interface_Serializer(framework);
        s2 = framework.shortNumber();
        s3 = framework.integer();
        s4 = framework.longNumber();
        s5 = framework.floatNumber();
        s6 = framework.doubleNumber();
        s7 = framework.bool();
        s8 = framework.uuid();
        s9 = framework.byteArray();
        s10 = framework.charArray();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Fields value) throws IOException {
        s0.serialize(buffer, value.string());
        s1.serialize(buffer, value.requiredCustom());
        s2.serialize(buffer, value.shortNumber());
        s3.serialize(buffer, value.integer());
        s4.serialize(buffer, value.longNumber());
        s5.serialize(buffer, value.floatNumber());
        s6.serialize(buffer, value.doubleNumber());
        s7.serialize(buffer, value.bool());
        s8.serialize(buffer, value.uuid());
        s9.serialize(buffer, value.byteArray());
        s10.serialize(buffer, value.charArray());
    }

    @Override
    public Fields deserialize(final SerialReader buffer) throws IOException {
        final String v0 = s0.deserialize(buffer);
        final Interface v1 = s1.deserialize(buffer);
        final short v2 = s2.deserialize(buffer);
        final int v3 = s3.deserialize(buffer);
        final long v4 = s4.deserialize(buffer);
        final float v5 = s5.deserialize(buffer);
        final double v6 = s6.deserialize(buffer);
        final boolean v7 = s7.deserialize(buffer);
        final UUID v8 = s8.deserialize(buffer);
        final byte[] v9 = s9.deserialize(buffer);
        final char[] v10 = s10.deserialize(buffer);

        return new Fields(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10);
    }
}