package eu.toolchain.serializer;

import eu.toolchain.serializer.collection.DefaultListSerializer;
import eu.toolchain.serializer.collection.DefaultMapSerializer;
import eu.toolchain.serializer.collection.DefaultNavigableMapSerializer;
import eu.toolchain.serializer.collection.DefaultNavigableSetSerializer;
import eu.toolchain.serializer.collection.DefaultSetSerializer;
import eu.toolchain.serializer.collection.DefaultSortedMapSerializer;
import eu.toolchain.serializer.collection.DefaultSortedSetSerializer;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultCollectionsProvider implements CollectionsProvider {
  final Serializer<Integer> size;

  @Override
  public <T> Serializer<List<T>> list(Serializer<T> value) {
    return new DefaultListSerializer<T>(size, value);
  }

  @Override
  public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value) {
    return new DefaultMapSerializer<K, V>(size, key, value);
  }

  @Override
  public <K extends Comparable<?>, V> Serializer<SortedMap<K, V>> sortedMap(
    Serializer<K> key, Serializer<V> value
  ) {
    return new DefaultSortedMapSerializer<K, V>(size, key, value);
  }

  @Override
  public <K extends Comparable<?>, V> Serializer<NavigableMap<K, V>> navigableMap(
    Serializer<K> key, Serializer<V> value
  ) {
    return new DefaultNavigableMapSerializer<K, V>(size, key, value);
  }

  @Override
  public <T> Serializer<Set<T>> set(Serializer<T> value) {
    return new DefaultSetSerializer<T>(size, value);
  }

  @Override
  public <T extends Comparable<?>> Serializer<SortedSet<T>> sortedSet(Serializer<T> value) {
    return new DefaultSortedSetSerializer<T>(size, value);
  }

  @Override
  public <T extends Comparable<?>> Serializer<NavigableSet<T>> navigableSet(
    Serializer<T> serializer
  ) {
    return new DefaultNavigableSetSerializer<T>(size, serializer);
  }
}
