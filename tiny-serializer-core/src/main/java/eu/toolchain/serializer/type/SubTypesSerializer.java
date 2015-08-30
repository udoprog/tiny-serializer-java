package eu.toolchain.serializer.type;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework.TypeMapping;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubTypesSerializer<T> implements Serializer<T> {
    final Serializer<Integer> typeId;
    final Map<Integer, TypeMapping<? extends T, T>> ids;
    final Map<Class<? extends T>, TypeMapping<? extends T, T>> keys;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        final TypeMapping<? extends T, T> m = keys.get(value.getClass());

        if (m == null) {
            throw new IllegalArgumentException("Type not supported: " + value.getClass());
        }

        typeId.serialize(buffer, m.id());
        @SuppressWarnings("unchecked")
        final Serializer<T> serializer = (Serializer<T>) m.serializer();
        serializer.serialize(buffer, value);
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        final int id = typeId.deserialize(buffer);
        final TypeMapping<? extends T, T> m = ids.get(id);

        if (m == null) {
            throw new IllegalArgumentException("Type id not supported: " + id);
        }

        return m.serializer().deserialize(buffer);
    }

    public static <T> Serializer<T> fromTypeMappings(Serializer<Integer> typeId, Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        final Map<Integer, TypeMapping<? extends T, T>> ids = buildIdMapping(mappings);
        final Map<Class<? extends T>, TypeMapping<? extends T, T>> keys = buildTypeMapping(mappings);
        return new SubTypesSerializer<T>(typeId, ids, keys);
    }

    static <T> Map<Integer, TypeMapping<? extends T, T>> buildIdMapping(
            Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        final Map<Integer, TypeMapping<? extends T, T>> mapping = new HashMap<>();

        for (TypeMapping<? extends T, T> m : mappings) {
            if (mapping.put(m.id(), m) == null)
                continue;

            throw new IllegalArgumentException("Duplicate mappings for " + m);
        }

        return mapping;
    }

    static <T> Map<Class<? extends T>, TypeMapping<? extends T, T>> buildTypeMapping(
            Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        final Map<Class<? extends T>, TypeMapping<? extends T, T>> mapping = new HashMap<>();

        for (final TypeMapping<? extends T, T> m : mappings) {
            if (mapping.put(m.key(), m) == null)
                continue;

            throw new IllegalArgumentException("Duplicate mappings for " + m);
        }

        return mapping;
    }
}
