package eu.toolchain.serializer.processor.field;

import lombok.Data;

@Data
public class Value {
  private final Field type;
  private final String name;
  private final String accessor;
  private final String variableName;
  private final String isSetVariableName;
  private final boolean valueProvided;
}
