package eu.toolchain.serializer.processor.field;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FieldSet {
  private final List<FieldType> types;
  private final List<Field> fields;

  public List<String> getVariableNames() {
    return fields.stream().map(Field::getVariableName).collect(Collectors.toList());
  }
}
