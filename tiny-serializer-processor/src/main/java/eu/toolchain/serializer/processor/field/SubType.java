package eu.toolchain.serializer.processor.field;

import javax.lang.model.type.DeclaredType;
import lombok.Data;

@Data
public class SubType {
  final DeclaredType type;
  final short id;
}
