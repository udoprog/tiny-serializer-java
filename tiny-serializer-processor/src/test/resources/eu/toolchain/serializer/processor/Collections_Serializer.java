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
    final Serializer<List<Interface>> s_List;
    final Serializer<Map<String, Interface>> s_Map;
    final Serializer<SortedMap<String, Interface>> s_SortedMap;
    final Serializer<Set<Interface>> s_Set;
    final Serializer<SortedSet<Interface>> s_SortedSet;

    public Collections_Serializer(final SerializerFramework framework) {
        s_List = framework.list(new Interface_Serializer(framework));
        s_Map = framework.map(framework.string(), new Interface_Serializer(framework));
        s_SortedMap = framework.sortedMap(framework.string(), new Interface_Serializer(framework));
        s_Set = framework.set(new Interface_Serializer(framework));
        s_SortedSet = framework.sortedSet(new Interface_Serializer(framework));
    }

    @Override
    public void serialize(final SerialWriter buffer, final Collections value) throws IOException {
        s_List.serialize(buffer, value.getList());
        s_Map.serialize(buffer, value.getMap());
        s_SortedMap.serialize(buffer, value.getSortedMap());
        s_Set.serialize(buffer, value.getSet());
        s_SortedSet.serialize(buffer, value.getSortedSet());
    }

    @Override
    public Collections deserialize(final SerialReader buffer) throws IOException {
        final List<Interface> v_list = s_List.deserialize(buffer);
        final Map<String, Interface> v_map = s_Map.deserialize(buffer);
        final SortedMap<String, Interface> v_sortedMap = s_SortedMap.deserialize(buffer);
        final Set<Interface> v_set = s_Set.deserialize(buffer);
        final SortedSet<Interface> v_sortedSet = s_SortedSet.deserialize(buffer);
        return new Collections(v_list, v_map, v_sortedMap, v_set, v_sortedSet);
    }
}