package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class ImplB_Serializer implements Serializer<ImplB> {
    public ImplB_Serializer(final SerializerFramework framework) {
    }

    @Override
    public void serialize(final SerialWriter buffer, final ImplB value) throws IOException {
    }

    @Override
    public ImplB deserialize(final SerialReader buffer) throws IOException {
        return new ImplB();
    }
}
