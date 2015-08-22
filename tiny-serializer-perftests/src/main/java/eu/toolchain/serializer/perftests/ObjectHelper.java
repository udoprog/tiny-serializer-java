package eu.toolchain.serializer.perftests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ObjectHelper {
    public static SerializedObject newSerializedObject() {
        return new SerializedObject(42, "hello world", ImmutableMap.of("hello", "world", "this", "sucks"),
                ImmutableList.of("fee", "fii", "foo", "fum"));
    }

    public static AutoMatterSerializedObject newAutoMatterSerializedObject() {
        return new AutoMatterSerializedObjectBuilder().version(42).field("hello world").map(ImmutableMap.of("hello", "world", "this", "sucks")).someStrings(
                ImmutableList.of("fee", "fii", "foo", "fum")).build();
    }

    public static MutableSerializedObject newMutableSerializedObject() {
        final HashMap<String, String> map = new HashMap<>(ImmutableMap.of("hello", "world", "this",
                "sucks"));
        final List<String> list = new ArrayList<>(ImmutableList.of("fee", "fii", "foo", "fum"));
        return new MutableSerializedObject(42, "hello world", map, list);
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