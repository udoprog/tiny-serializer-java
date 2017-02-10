package eu.toolchain.serializer;

import eu.toolchain.serializer.collection.ImmutableListSerializer;
import eu.toolchain.serializer.collection.ImmutableMapSerializer;
import eu.toolchain.serializer.collection.ImmutableNavigableMapSerializer;
import eu.toolchain.serializer.collection.ImmutableNavigableSetSerializer;
import eu.toolchain.serializer.collection.ImmutableSetSerializer;
import eu.toolchain.serializer.collection.ImmutableSortedMapSerializer;
import eu.toolchain.serializer.collection.ImmutableSortedSetSerializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public class ImmutableCollectionsProvider implements CollectionsProvider {
  private final Serializer<Integer> size;

  public ImmutableCollectionsProvider(final Serializer<Integer> size) {
    this.size = size;
  }

  static final String[] collections = new String[]{
    "ImmutableList", "ImmutableMap", "ImmutableSortedMap", "ImmutableNavigableMap", "ImmutableSet",
    "ImmutableSortedSet", "ImmutableNavigableSet"
  };

  public static void verifyGuavaAvailable() {
    final List<String> missing = new ArrayList<>();

    for (final String collection : collections) {
      if (!check(String.format("com.google.common.collect.%s", collection))) {
        missing.add(collection);
      }
    }

    if (!missing.isEmpty()) {
      throw new IllegalStateException(
        String.format("Missing guava collections (%s), is guava available in your classpath?",
          missing));
    }
  }

  static boolean check(String className) {
    try {
      Class.forName(className, true, ImmutableCollectionsProvider.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      return false;
    }

    return true;
  }

  @Override
  public <T> Serializer<List<T>> list(Serializer<T> value) {
    return new ImmutableListSerializer<T>(size, value);
  }

  @Override
  public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value) {
    return new ImmutableMapSerializer<K, V>(size, key, value);
  }

  @Override
  public <K extends Comparable<?>, V> Serializer<SortedMap<K, V>> sortedMap(
    Serializer<K> key, Serializer<V> value
  ) {
    return new ImmutableSortedMapSerializer<K, V>(size, key, value);
  }

  @Override
  public <K extends Comparable<?>, V> Serializer<NavigableMap<K, V>> navigableMap(
    Serializer<K> key, Serializer<V> value
  ) {
    return new ImmutableNavigableMapSerializer<K, V>(size, key, value);
  }

  @Override
  public <T> Serializer<Set<T>> set(Serializer<T> value) {
    return new ImmutableSetSerializer<T>(size, value);
  }

  @Override
  public <T extends Comparable<?>> Serializer<SortedSet<T>> sortedSet(Serializer<T> value) {
    return new ImmutableSortedSetSerializer<T>(size, value);
  }

  @Override
  public <T extends Comparable<?>> Serializer<NavigableSet<T>> navigableSet(Serializer<T> value) {
    return new ImmutableNavigableSetSerializer<T>(size, value);
  }
}
