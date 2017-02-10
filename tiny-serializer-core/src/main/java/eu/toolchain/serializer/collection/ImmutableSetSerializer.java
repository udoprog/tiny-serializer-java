package eu.toolchain.serializer.collection;

import com.google.common.collect.ImmutableSet;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImmutableSetSerializer<T> implements Serializer<Set<T>> {
  private final Serializer<Integer> size;
  private final Serializer<T> serializer;

  @Override
  public void serialize(SerialWriter buffer, Set<T> values) throws IOException {
    size.serialize(buffer, values.size());

    for (final T value : values) {
      serializer.serialize(buffer, value);
    }
  }

  @Override
  public Set<T> deserialize(SerialReader buffer) throws IOException {
    final int size = this.size.deserialize(buffer);

    final ImmutableSet.Builder<T> values = ImmutableSet.builder();

    for (int i = 0; i < size; ++i) {
      values.add(serializer.deserialize(buffer));
    }

    return values.build();
  }
}
