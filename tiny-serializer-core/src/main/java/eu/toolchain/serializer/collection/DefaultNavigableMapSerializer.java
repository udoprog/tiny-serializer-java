package eu.toolchain.serializer.collection;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultNavigableMapSerializer<K extends Comparable<?>, V> implements Serializer<NavigableMap<K, V>> {
    private final Serializer<Integer> integer;

    private final Serializer<K> key;
    private final Serializer<V> value;

    @Override
    public void serialize(SerialWriter buffer, NavigableMap<K, V> values) throws IOException {
        integer.serialize(buffer, values.size());

        for (final Map.Entry<K, V> entry : values.entrySet()) {
            key.serialize(buffer, entry.getKey());
            value.serialize(buffer, entry.getValue());
        }
    }

    @Override
    public NavigableMap<K, V> deserialize(SerialReader buffer) throws IOException {
        final int size = integer.deserialize(buffer);

        final NavigableMap<K, V> values = new TreeMap<>();

        for (int i = 0; i < size; ++i) {
            final K key = this.key.deserialize(buffer);
            final V value = this.value.deserialize(buffer);
            values.put(key, value);
        }

        return values;
    }
}
