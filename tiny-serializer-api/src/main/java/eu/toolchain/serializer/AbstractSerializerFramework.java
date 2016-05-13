package eu.toolchain.serializer;

public abstract class AbstractSerializerFramework implements SerializerFramework {
    @Override
    public Serializer<Boolean> bool() {
        return fixedBoolean();
    }

    @Override
    public Serializer<Short> shortNumber() {
        return fixedShort();
    }

    @Override
    public Serializer<Integer> integer() {
        return fixedInteger();
    }

    @Override
    public Serializer<Long> longNumber() {
        return fixedLong();
    }

    @Override
    public Serializer<Float> floatNumber() {
        return fixedFloat();
    }

    @Override
    public Serializer<Double> doubleNumber() {
        return fixedDouble();
    }

    @Override
    public Serializer<Integer> varint() {
        return variableInteger();
    }

    @Override
    public Serializer<Long> varlong() {
        return variableLong();
    }

    ;
}
