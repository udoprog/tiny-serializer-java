package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Collections {
    final List<Interface> list;
    final Map<String, Interface> map;
    final SortedMap<String, Interface> sortedMap;
    final NavigableMap<String, Interface> navigableMap;
    final Set<Interface> set;
    final SortedSet<Interface> sortedSet;
    final NavigableSet<Interface> navigableSet;

    public Collections(List<Interface> list, Map<String, Interface> map, SortedMap<String, Interface> sortedMap,
            NavigableMap<String, Interface> navigableMap, Set<Interface> set, SortedSet<Interface> sortedSet,
            NavigableSet<Interface> navigableSet) {
        this.list = list;
        this.map = map;
        this.sortedMap = sortedMap;
        this.navigableMap = navigableMap;
        this.set = set;
        this.sortedSet = sortedSet;
        this.navigableSet = navigableSet;
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

    public NavigableMap<String, Interface> getNavigableMap() {
        return navigableMap;
    }

    public Set<Interface> getSet() {
        return set;
    }

    public SortedSet<Interface> getSortedSet() {
        return sortedSet;
    }

    public NavigableSet<Interface> getNavigableSet() {
        return navigableSet;
    }
}