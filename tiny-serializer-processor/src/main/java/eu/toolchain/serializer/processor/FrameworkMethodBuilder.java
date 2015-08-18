package eu.toolchain.serializer.processor;

import java.util.List;

public interface FrameworkMethodBuilder {
    void assign(String statement, List<Object> arguments);
}