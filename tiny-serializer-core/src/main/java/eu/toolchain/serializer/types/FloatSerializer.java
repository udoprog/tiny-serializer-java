package eu.toolchain.serializer.types;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FloatSerializer implements Serializer<Float> {
    private final Serializer<Integer> intS;

    @Override
    public void serialize(SerialWriter buffer, Float value) throws IOException {
        intS.serialize(buffer, Float.floatToIntBits(value));
    }

    @Override
    public Float deserialize(SerialReader buffer) throws IOException {
        return Float.intBitsToFloat(intS.deserialize(buffer));
    }
}