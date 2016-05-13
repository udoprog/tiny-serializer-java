package eu.toolchain.serializer.perftests;

import eu.toolchain.serializer.AutoSerialize;
import io.norberg.automatter.AutoMatter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoSerialize(builder = @AutoSerialize.Builder(type = AutoMatterSerializedObjectBuilder.class))
@AutoMatter
public interface AutoMatterSerializedObject {
    public int version();

    public String field();

    public Map<String, String> map();

    public List<String> list();

    public Map<String, List<String>> optionalMap();

    public Set<Long> set();

    public int[][][] deeplyNested();
}
