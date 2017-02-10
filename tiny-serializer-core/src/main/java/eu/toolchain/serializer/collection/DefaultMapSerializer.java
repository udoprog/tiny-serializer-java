package eu.toolchain.serializer.collection;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultMapSerializer<K, V> implements Serializer<Map<K, V>> {
  private final Serializer<Integer> integer;

  private final Serializer<K> key;
  private final Serializer<V> value;

  @Override
  public void serialize(SerialWriter buffer, Map<K, V> values) throws IOException {
    integer.serialize(buffer, values.size());

    for (final Map.Entry<K, V> entry : values.entrySet()) {
      key.serialize(buffer, entry.getKey());
      value.serialize(buffer, entry.getValue());
    }
  }

  @Override
  public Map<K, V> deserialize(SerialReader buffer) throws IOException {
    final int size = integer.deserialize(buffer);

    final Map<K, V> values = new HashMap<>();

    for (int i = 0; i < size; ++i) {
      final K key = this.key.deserialize(buffer);
      final V value = this.value.deserialize(buffer);
      values.put(key, value);
    }

    return values;
  }
}
