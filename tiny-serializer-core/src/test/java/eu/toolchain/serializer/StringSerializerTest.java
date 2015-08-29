package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.toolchain.serializer.types.CompactVarIntSerializer;
import eu.toolchain.serializer.types.StringSerializer;

public class StringSerializerTest {
    private Serializer<String> string;

    @Before
    public void setup() {
        string = new StringSerializer(new CompactVarIntSerializer());
    }

    @Test
    public void basicTests() throws IOException {
        Helpers.roundtrip(string, "Hello World");
        Helpers.roundtrip(string, "Hello: åäö");
    }
}