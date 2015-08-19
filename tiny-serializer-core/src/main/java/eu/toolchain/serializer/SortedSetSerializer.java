package eu.toolchain.serializer;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SortedSetSerializer<T> implements Serializer<SortedSet<T>> {
    private final Serializer<Integer> size;
    private final Serializer<T> serializer;

    @Override
    public void serialize(SerialWriter buffer, SortedSet<T> values) throws IOException {
        size.serialize(buffer, values.size());

        for (final T value : values)
            serializer.serialize(buffer, value);
    }

    @Override
    public SortedSet<T> deserialize(SerialReader buffer) throws IOException {
        final int size = this.size.deserialize(buffer);

        final SortedSet<T> values = new TreeSet<>();

        for (int i = 0; i < size; ++i)
            values.add(serializer.deserialize(buffer));

        return values;
    }
}
