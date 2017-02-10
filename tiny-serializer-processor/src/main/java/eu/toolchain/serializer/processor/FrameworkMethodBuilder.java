package eu.toolchain.serializer.processor;

import java.util.List;

@FunctionalInterface
public interface FrameworkMethodBuilder {
  void assign(String statement, List<Object> arguments);
}
