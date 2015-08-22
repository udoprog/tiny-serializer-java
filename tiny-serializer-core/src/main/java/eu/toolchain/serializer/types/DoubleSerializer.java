package eu.toolchain.serializer.types;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DoubleSerializer implements Serializer<Double> {
    private final Serializer<Long> longS;

    @Override
    public void serialize(SerialWriter buffer, Double value) throws IOException {
        longS.serialize(buffer, Double.doubleToLongBits(value));
    }

    @Override
    public Double deserialize(SerialReader buffer) throws IOException {
        return Double.longBitsToDouble(longS.deserialize(buffer));
    }
}