package eu.toolchain.serializer.perftests;

import java.util.List;
import java.util.Map;

import lombok.Data;
import eu.toolchain.serializer.AutoSerialize;


@Data
@AutoSerialize
public class SerializedObject {
    @AutoSerialize.Field(id = 1)
    final int version;

    @AutoSerialize.Field(id = 2)
    final String field;

    @AutoSerialize.Field(id = 3)
    final Map<String, String> map;

    @AutoSerialize.Field(id = 4)
    final List<String> someStrings;
}