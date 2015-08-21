package eu.toolchain.serializer.perftests;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutableSerializedObject implements Serializable {
    int version;
    String field;
    Map<String, String> map;
    List<String> someStrings;
}