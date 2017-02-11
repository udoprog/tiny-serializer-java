package eu.toolchain.serializer.processor.field;

import java.util.List;
import javax.lang.model.type.DeclaredType;
import lombok.Data;

@Data
public class SubType {
  private final DeclaredType type;
  private final short id;
  private final List<Field> fields;
  private final List<Value> values;
}
