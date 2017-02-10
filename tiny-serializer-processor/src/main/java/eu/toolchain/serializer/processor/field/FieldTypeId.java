package eu.toolchain.serializer.processor.field;

import com.squareup.javapoet.TypeName;
import java.util.Optional;
import lombok.Data;

@Data
class FieldTypeId {
  private final TypeName fieldType;
  private final boolean provided;
  private final Optional<String> providerName;
}
