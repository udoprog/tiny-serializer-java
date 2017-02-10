package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Ignore {
    final String visible;

    @AutoSerialize.Ignore
    final String hidden;

    public Ignore(final String visible) {
        this.visible = visible;
        this.hidden = visible.toUpperCase();
    }

    public String getVisible() {
        return visible;
    }
}
