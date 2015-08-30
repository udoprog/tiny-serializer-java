package eu.toolchain.serializer.array;

import java.io.IOException;

import eu.toolchain.serializer.ArrayConstructor;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArraySerializer<T> implements Serializer<T[]> {
    private final Serializer<Integer> size;
    private final Serializer<T> element;
    private final ArrayConstructor<T> constructor;

    @Override
    public void serialize(SerialWriter buffer, T[] value) throws IOException {
        this.size.serialize(buffer, value.length);

        for (final T v : value) {
            this.element.serialize(buffer, v);
        }
    }

    @Override
    public T[] deserialize(SerialReader buffer) throws IOException {
        final int size = this.size.deserialize(buffer);
        final T[] array = constructor.newArray(size);

        for (int i = 0; i < size; i++) {
            array[i] = this.element.deserialize(buffer);
        }

        return array;
    }
}