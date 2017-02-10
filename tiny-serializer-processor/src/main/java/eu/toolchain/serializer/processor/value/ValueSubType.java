package eu.toolchain.serializer.processor.value;

import javax.lang.model.type.DeclaredType;
import lombok.Data;

@Data
public class ValueSubType {
  final DeclaredType type;
  final short id;
}
