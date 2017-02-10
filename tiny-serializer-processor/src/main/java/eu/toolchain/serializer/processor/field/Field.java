package eu.toolchain.serializer.processor.field;

import java.util.Optional;
import lombok.Data;

@Data
public class Field {
  private final FieldType type;
  private final String name;
  private final String accessor;
  private final String variableName;
  private final String isSetVariableName;
  private final Optional<Integer> id;
  private final Optional<Integer> constructorOrder;
  private final boolean valueProvided;
}
