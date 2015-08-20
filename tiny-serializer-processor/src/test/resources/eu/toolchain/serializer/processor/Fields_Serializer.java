package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import java.util.UUID;

public final class Fields_Serializer implements Serializer<Fields> {
    final Serializer<String> s_String;
    final Serializer<Interface> s_Interface;
    final Serializer<Short> s_Short;
    final Serializer<Integer> s_Integer;
    final Serializer<Long> s_Long;
    final Serializer<Float> s_Float;
    final Serializer<Double> s_Double;
    final Serializer<Boolean> s_Boolean;
    final Serializer<UUID> s_UUID;
    final Serializer<byte[]> s_ByteArray;
    final Serializer<char[]> s_CharacterArray;

    public Fields_Serializer(final SerializerFramework framework) {
        s_String = framework.string();
        s_Interface = new Interface_Serializer(framework);
        s_Short = framework.shortNumber();
        s_Integer = framework.integer();
        s_Long = framework.longNumber();
        s_Float = framework.floatNumber();
        s_Double = framework.doubleNumber();
        s_Boolean = framework.bool();
        s_UUID = framework.uuid();
        s_ByteArray = framework.byteArray();
        s_CharacterArray = framework.charArray();
    }

    @Override
    public void serialize(final SerialWriter buffer, final Fields value) throws IOException {
        s_String.serialize(buffer, value.string());
        s_Interface.serialize(buffer, value.requiredCustom());
        s_Short.serialize(buffer, value.shortNumber());
        s_Integer.serialize(buffer, value.integer());
        s_Long.serialize(buffer, value.longNumber());
        s_Float.serialize(buffer, value.floatNumber());
        s_Double.serialize(buffer, value.doubleNumber());
        s_Boolean.serialize(buffer, value.bool());
        s_UUID.serialize(buffer, value.uuid());
        s_ByteArray.serialize(buffer, value.byteArray());
        s_CharacterArray.serialize(buffer, value.charArray());
    }

    @Override
    public Fields deserialize(final SerialReader buffer) throws IOException {
        final String v_string = s_String.deserialize(buffer);
        final Interface v_requiredCustom = s_Interface.deserialize(buffer);
        final short v_shortNumber = s_Short.deserialize(buffer);
        final int v_integer = s_Integer.deserialize(buffer);
        final long v_longNumber = s_Long.deserialize(buffer);
        final float v_floatNumber = s_Float.deserialize(buffer);
        final double v_doubleNumber = s_Double.deserialize(buffer);
        final boolean v_bool = s_Boolean.deserialize(buffer);
        final UUID v_uuid = s_UUID.deserialize(buffer);
        final byte[] v_byteArray = s_ByteArray.deserialize(buffer);
        final char[] v_charArray= s_CharacterArray.deserialize(buffer);

        return new Fields(v_string, v_requiredCustom, v_shortNumber, v_integer, v_longNumber, v_floatNumber, v_doubleNumber, v_bool, v_uuid, v_byteArray, v_charArray);
    }
}