package eu.toolchain.serializer.perftests;

import java.util.List;
import java.util.Map;

import eu.toolchain.serializer.AutoSerialize;
import io.norberg.automatter.AutoMatter;

@AutoSerialize(builder = @AutoSerialize.Builder(type = AutoMatterSerializedObjectBuilder.class))
@AutoMatter
public interface AutoMatterSerializedObject {
    public int version();
    public String field();
    public Map<String, String> map();
    public List<String> someStrings();
}