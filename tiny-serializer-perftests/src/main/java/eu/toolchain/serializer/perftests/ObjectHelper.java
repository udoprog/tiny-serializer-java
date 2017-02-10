package eu.toolchain.serializer.perftests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ObjectHelper {
  public static ImmutableSerializedObject newSerializedObject() {
    final Map<String, String> map = newMap();
    final List<String> list = newList();
    final Map<String, List<String>> optionalMap = newOptionalMap();
    final Set<Long> set = newSet();
    final int[][][] deeplyNested = newDeeplyNested();
    return new ImmutableSerializedObject(42, "hello world", map, list, optionalMap, set,
      deeplyNested);
  }

  public static AutoMatterSerializedObject newAutoMatterSerializedObject() {
    final Map<String, String> map = newMap();
    final List<String> list = newList();
    final Map<String, List<String>> optionalMap = newOptionalMap();
    final Set<Long> set = newSet();
    final int[][][] deeplyNested = newDeeplyNested();
    return new AutoMatterSerializedObjectBuilder()
      .version(42)
      .field("hello world")
      .map(map)
      .list(list)
      .optionalMap(optionalMap)
      .set(set)
      .deeplyNested(deeplyNested)
      .build();
  }

  public static MutableSerializedObject newMutableSerializedObject() {
    final Map<String, String> map = new HashMap<>(newMap());
    final List<String> list = new ArrayList<>(newList());
    final Map<String, List<String>> optionalMap = new HashMap<>(newOptionalMap());
    final Set<Long> set = new HashSet<>(newSet());
    final int[][][] deeplyNested = newDeeplyNested();
    return new MutableSerializedObject(42, "hello world", map, list, optionalMap, set,
      deeplyNested);
  }

  private static Map<String, List<String>> newOptionalMap() {
    return ImmutableMap.of("a", ImmutableList.of("c", "b"), "c", ImmutableList.of("e", "f"));
  }

  private static Map<String, String> newMap() {
    return ImmutableMap.of("hello", "world", "this", "sucks", "long string", "another long string",
      "an even longer string with a twist", "ok");
  }

  private static List<String> newList() {
    return ImmutableList.of("fee", "fii", "foo", "fum", "another long string",
      "an even longer string with a twist");
  }

  private static Set<Long> newSet() {
    ImmutableSet.Builder<Long> set = ImmutableSet.builder();
    long v = 1;

    for (long i = 0; i < 100; i++) {
      set.add(v);
      v = (v << 5) + 1;
    }

    return set.build();
  }

  public static Supplier<InputStream> supplyInputStreamFrom(Callable<byte[]> bytesSupplier) {
    try {
      final byte[] bytes = bytesSupplier.call();
      return () -> new ByteArrayInputStream(bytes);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static int[][][] newDeeplyNested() {
    return new int[][][]{{{1, 2, 3}, {4, 5, 6}}};
  }
}
