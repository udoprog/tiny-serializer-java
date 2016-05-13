package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class UseBuilder_Serializer implements Serializer<UseBuilder> {
    final Serializer<String> s_String;

    public UseBuilder_Serializer(final SerializerFramework framework) {
        s_String = framework.string();
    }

    @Override
    public void serialize(final SerialWriter buffer, final UseBuilder value) throws IOException {
        s_String.serialize(buffer, value.getString());
    }

    @Override
    public UseBuilder deserialize(final SerialReader buffer) throws IOException {
        final String v_string = s_String.deserialize(buffer);
        return UseBuilder.builder().setString(v_string).build();
    }
}
