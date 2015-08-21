package eu.toolchain.serializer.perftests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ObjectHelper {
    public static SerializedObject newSerializedObject() {
        return new SerializedObject(42, "hello world", ImmutableMap.of("hello", "world", "this", "sucks"),
                ImmutableList.of("fee", "fii", "foo", "fum"));
    }

    public static SerializableSerializedObject newSerializableSerializedObject() {
        return new SerializableSerializedObject(42, "hello world", ImmutableMap.of("hello", "world", "this", "sucks"),
                ImmutableList.of("fee", "fii", "foo", "fum"));
    }

    public static Supplier<InputStream> supplyInputStreamFrom(Callable<byte[]> bytesSupplier) {
        try {
            final byte[] bytes = bytesSupplier.call();
            return () -> new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}