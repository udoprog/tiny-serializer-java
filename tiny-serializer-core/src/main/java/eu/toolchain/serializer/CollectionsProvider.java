package eu.toolchain.serializer;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public interface CollectionsProvider {
    public <T> Serializer<List<T>> list(Serializer<T> value);

    public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value);

    public <K extends Comparable<?>, V> Serializer<SortedMap<K, V>> sortedMap(Serializer<K> key, Serializer<V> value);

    public <K extends Comparable<?>, V> Serializer<NavigableMap<K, V>> navigableMap(Serializer<K> key, Serializer<V> value);

    public <T> Serializer<Set<T>> set(Serializer<T> value);

    public <T extends Comparable<?>> Serializer<SortedSet<T>> sortedSet(Serializer<T> value);

    public <T extends Comparable<?>> Serializer<NavigableSet<T>> navigableSet(Serializer<T> serializer);
}