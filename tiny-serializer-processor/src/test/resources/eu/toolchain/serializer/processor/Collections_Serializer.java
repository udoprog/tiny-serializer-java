package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public final class Collections_Serializer implements Serializer<Collections> {
    final Serializer<List<Interface>> s0;
    final Serializer<Map<String, Interface>> s1;
    final Serializer<SortedMap<String, Interface>> s2;
    final Serializer<Set<Interface>> s3;
    final Serializer<SortedSet<Interface>> s4;

    public Collections_Serializer(final SerializerFramework framework) {
        s0 = framework.list(new Interface_Serializer(framework));
        s1 = framework.map(framework.string(), new Interface_Serializer(framework));
        s2 = framework.sortedMap(framework.string(), new Interface_Serializer(framework));
        s3 = framework.set(new Interface_Serializer(framework));
        s4 = framework.sortedSet(new Interface_Serializer(framework));
    }

    @Override
    public void serialize(final SerialWriter buffer, final Collections value) throws IOException {
        s0.serialize(buffer, value.getList());
        s1.serialize(buffer, value.getMap());
        s2.serialize(buffer, value.getSortedMap());
        s3.serialize(buffer, value.getSet());
        s4.serialize(buffer, value.getSortedSet());
    }

    @Override
    public Collections deserialize(final SerialReader buffer) throws IOException {
        final List<Interface> v0 = s0.deserialize(buffer);
        final Map<String, Interface> v1 = s1.deserialize(buffer);
        final SortedMap<String, Interface> v2 = s2.deserialize(buffer);
        final Set<Interface> v3 = s3.deserialize(buffer);
        final SortedSet<Interface> v4 = s4.deserialize(buffer);
        return new Collections(v0, v1, v2, v3, v4);
    }
}