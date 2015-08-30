package eu.toolchain.serializer.type;

import java.io.IOException;
import java.util.Optional;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OptionalSerializer<T> implements Serializer<Optional<T>> {
    final Serializer<Boolean> bool;
    final Serializer<T> element;

    @Override
    public void serialize(SerialWriter buffer, Optional<T> value) throws IOException {
        if (value.isPresent()) {
            bool.serialize(buffer, true);
            element.serialize(buffer, value.get());
        } else {
            bool.serialize(buffer, false);
        }
    }

    @Override
    public Optional<T> deserialize(SerialReader buffer) throws IOException {
        if (bool.deserialize(buffer)) {
            return Optional.of(element.deserialize(buffer));
        }

        return Optional.empty();
    }
}