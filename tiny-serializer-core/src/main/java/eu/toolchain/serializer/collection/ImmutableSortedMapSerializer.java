package eu.toolchain.serializer.collection;

import com.google.common.collect.ImmutableSortedMap;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;

@RequiredArgsConstructor
public class ImmutableSortedMapSerializer<K extends Comparable<?>, V>
    implements Serializer<SortedMap<K, V>> {
    private final Serializer<Integer> size;

    private final Serializer<K> key;
    private final Serializer<V> value;

    @Override
    public void serialize(SerialWriter buffer, SortedMap<K, V> values) throws IOException {
        size.serialize(buffer, values.size());

        for (final Map.Entry<K, V> entry : values.entrySet()) {
            key.serialize(buffer, entry.getKey());
            value.serialize(buffer, entry.getValue());
        }
    }

    @Override
    public SortedMap<K, V> deserialize(SerialReader buffer) throws IOException {
        final int size = this.size.deserialize(buffer);

        final ImmutableSortedMap.Builder<K, V> values = ImmutableSortedMap.naturalOrder();

        for (int i = 0; i < size; ++i) {
            final K key = this.key.deserialize(buffer);
            final V value = this.value.deserialize(buffer);
            values.put(key, value);
        }

        return values.build();
    }
}
