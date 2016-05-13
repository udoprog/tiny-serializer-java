package eu.toolchain.serializer.collection;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.NavigableSet;
import java.util.TreeSet;

@RequiredArgsConstructor
public class DefaultNavigableSetSerializer<T extends Comparable<?>>
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

        final NavigableSet<T> values = new TreeSet<>();

        for (int i = 0; i < size; ++i) {
            values.add(serializer.deserialize(buffer));
        }

        return values;
    }
}
