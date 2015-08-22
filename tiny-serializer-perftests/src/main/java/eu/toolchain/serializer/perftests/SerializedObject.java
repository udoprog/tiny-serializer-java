package eu.toolchain.serializer.perftests;

import java.util.List;
import java.util.Map;

import lombok.Data;
import eu.toolchain.serializer.AutoSerialize;


@Data
@AutoSerialize
public class SerializedObject {
    final int version;
    final String field;
    final Map<String, String> map;
    final List<String> someStrings;
}