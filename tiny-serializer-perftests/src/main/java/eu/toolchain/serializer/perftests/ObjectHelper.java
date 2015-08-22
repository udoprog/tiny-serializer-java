package eu.toolchain.serializer.perftests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ObjectHelper {
    public static SerializedObject newSerializedObject() {
        final Map<String, String> map = newMap();
        final List<String> list = newList();
        return new SerializedObject(42, "hello world", map, list);
    }

    public static AutoMatterSerializedObject newAutoMatterSerializedObject() {
        final Map<String, String> map = newMap();
        final List<String> list = newList();
        return new AutoMatterSerializedObjectBuilder().version(42).field("hello world").map(map).someStrings(list)
                .build();
    }

    public static MutableSerializedObject newMutableSerializedObject() {
        final Map<String, String> map = new HashMap<>(newMap());
        final List<String> list = new ArrayList<>(newList());
        return new MutableSerializedObject(42, "hello world", map, list);
    }

    private static Map<String, String> newMap() {
        return ImmutableMap.of("hello", "world", "this", "sucks");
    }

    private static List<String> newList() {
        return ImmutableList.of("fee", "fii", "foo", "fum");
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