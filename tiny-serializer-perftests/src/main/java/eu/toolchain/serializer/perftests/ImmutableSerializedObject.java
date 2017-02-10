package eu.toolchain.serializer.perftests;

import eu.toolchain.serializer.AutoSerialize;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
@AutoSerialize
public class ImmutableSerializedObject {
  final int version;
  final String field;
  final Map<String, String> map;
  final List<String> list;
  final Map<String, List<String>> optionalMap;
  final Set<Long> set;
  final int[][][] deeplyNested;
}
