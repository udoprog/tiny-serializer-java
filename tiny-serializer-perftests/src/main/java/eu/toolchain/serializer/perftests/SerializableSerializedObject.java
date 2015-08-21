package eu.toolchain.serializer.perftests;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;


@Data
public class SerializableSerializedObject implements Serializable {
    final int version;
    final String field;
    final Map<String, String> map;
    final List<String> someStrings;
}