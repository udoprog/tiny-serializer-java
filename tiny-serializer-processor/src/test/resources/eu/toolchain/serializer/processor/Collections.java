package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Collections {
    final List<Interface> list;
    final Map<String, Interface> map;
    final SortedMap<String, Interface> sortedMap;
    final Set<Interface> set;
    final SortedSet<Interface> sortedSet;

    public Collections(List<Interface> list, Map<String, Interface> map, SortedMap<String, Interface> sortedMap,
            Set<Interface> set, SortedSet<Interface> sortedSet) {
        this.list = list;
        this.map = map;
        this.sortedMap = sortedMap;
        this.set = set;
        this.sortedSet = sortedSet;
    }

    public List<Interface> getList() {
        return list;
    }

    public Map<String, Interface> getMap() {
        return map;
    }

    public SortedMap<String, Interface> getSortedMap() {
        return sortedMap;
    }

    public Set<Interface> getSet() {
        return set;
    }

    public SortedSet<Interface> getSortedSet() {
        return sortedSet;
    }
}