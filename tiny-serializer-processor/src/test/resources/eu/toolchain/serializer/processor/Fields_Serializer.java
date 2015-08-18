package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AbsentProperty;
import eu.toolchain.serializer.OptionalProperty;
import eu.toolchain.serializer.PresentProperty;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class Fields_Serializer implements Serializer<Fields> {
    final Serializer<Boolean> optional;

    final Serializer<String> s0;
    final Serializer<Interface> s1;
    final Serializer<List<Interface>> s2;
    final Serializer<Map<String, Interface>> s3;
    final Serializer<Set<Interface>> s4;
    final Serializer<Short> s5;
    final Serializer<Integer> s6;
    final Serializer<Long> s7;
    final Serializer<Float> s8;
    final Serializer<Double> s9;
    final Serializer<Boolean> s10;
    final Serializer<UUID> s11;
    final Serializer<byte[]> s12;
    final Serializer<char[]> s13;

    public Fields_Serializer(final SerializerFramework framework) {
        optional = framework.bool();
        s0 = framework.string();
        s1 = new Interface_Serializer(framework);
        s2 = framework.list(new Interface_Serializer(framework));
        s3 = framework.map(framework.string(), new Interface_Serializer(framework));
        s4 = framework.set(new Interface_Serializer(framework));
        s5 = framework.shortNumber();
        s6 = framework.integer();
        s7 = framework.longNumber();
        s8 = framework.floatNumber();
        s9 = framework.doubleNumber();
        s10 = framework.bool();
        s11 = framework.uuid();
        s12 = framework.byteArray();
        s13 = framework.charArray();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Fields value) throws IOException {
        s0.serialize(buffer, value.string());
        s1.serialize(buffer, value.requiredCustom());

        {
            final OptionalProperty<Interface> o = value.optionalCustom();

            if (o.isPresent()) {
                optional.serialize(buffer, true);
                s1.serialize(buffer, o.get());
            } else {
                optional.serialize(buffer, false);
            }
        }

        s2.serialize(buffer, value.nestedCustom());
        s3.serialize(buffer, value.mappedCustom());
        s4.serialize(buffer, value.customSet());
        s5.serialize(buffer, value.shortNumber());
        s6.serialize(buffer, value.integer());
        s7.serialize(buffer, value.longNumber());
        s8.serialize(buffer, value.floatNumber());
        s9.serialize(buffer, value.doubleNumber());
        s10.serialize(buffer, value.bool());
        s11.serialize(buffer, value.uuid());
        s12.serialize(buffer, value.byteArray());
        s13.serialize(buffer, value.charArray());
    }

    @Override
    public Fields deserialize(final SerialReader buffer) throws IOException {
        final String v0 = s0.deserialize(buffer);
        final Interface v1 = s1.deserialize(buffer);

        final OptionalProperty<Interface> v2;

        if (optional.deserialize(buffer)) {
            v2 = new PresentProperty<Interface>(s1.deserialize(buffer));
        } else {
            v2 = AbsentProperty.absent();
        }

        final List<Interface> v3 = s2.deserialize(buffer);

        final Map<String, Interface> v4 = s3.deserialize(buffer);
        final Set<Interface> v5 = s4.deserialize(buffer);

        final short v6 = s5.deserialize(buffer);
        final int v7 = s6.deserialize(buffer);
        final long v8 = s7.deserialize(buffer);
        final float v9 = s8.deserialize(buffer);
        final double v10 = s9.deserialize(buffer);
        final boolean v11 = s10.deserialize(buffer);
        final UUID v12 = s11.deserialize(buffer);
        final byte[] v13 = s12.deserialize(buffer);
        final char[] v14 = s13.deserialize(buffer);

        return new Fields(v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }
}