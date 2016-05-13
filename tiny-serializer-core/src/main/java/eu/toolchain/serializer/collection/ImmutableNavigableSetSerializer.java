package eu.toolchain.serializer.collection;

import com.google.common.collect.ImmutableSortedSet;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.NavigableSet;

@RequiredArgsConstructor
public class ImmutableNavigableSetSerializer<T extends Comparable<?>>
    implements Serializer<NavigableSet<T>> {
    private final Serializer<Integer> size;
    private final Serializer<T> serializer;

    @Override
    public void serialize(SerialWriter buffer, NavigableSet<T> values) throws IOException {
        size.serialize(buffer, values.size());

        for (final T value : values) {
            serializer.serialize(buffer, value);
        }
    }

    @Override
    public NavigableSet<T> deserialize(SerialReader buffer) throws IOException {
        final int size = this.size.deserialize(buffer);

        final ImmutableSortedSet.Builder<T> values = ImmutableSortedSet.naturalOrder();

        for (int i = 0; i < size; ++i) {
            values.add(serializer.deserialize(buffer));
        }

        return values.build();
    }
}
