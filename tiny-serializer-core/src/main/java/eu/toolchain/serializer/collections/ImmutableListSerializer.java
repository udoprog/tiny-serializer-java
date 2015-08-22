package eu.toolchain.serializer.collections;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImmutableListSerializer<T> implements Serializer<List<T>> {
    private final Serializer<Integer> size;
    private final Serializer<T> serializer;

    @Override
    public void serialize(SerialWriter buffer, List<T> values) throws IOException {
        size.serialize(buffer, values.size());

        for (final T value : values) {
            serializer.serialize(buffer, value);
        }
    }

    @Override
    public List<T> deserialize(SerialReader buffer) throws IOException {
        final int size = this.size.deserialize(buffer);

        final ImmutableList.Builder<T> values = ImmutableList.builder();

        for (int i = 0; i < size; ++i) {
            values.add(serializer.deserialize(buffer));
        }

        return values.build();
    }
}
