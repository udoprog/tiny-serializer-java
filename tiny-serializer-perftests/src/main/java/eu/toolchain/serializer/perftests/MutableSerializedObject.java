package eu.toolchain.serializer.perftests;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.msgpack.annotation.Message;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Message
public class MutableSerializedObject implements Serializable {
    public int version;
    public String field;
    public Map<String, String> map;
    public List<String> someStrings;
}