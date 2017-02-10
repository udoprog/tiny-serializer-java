package eu.toolchain.serializer.processor.value;

import com.squareup.javapoet.TypeName;
import java.util.Optional;
import lombok.Data;

@Data
class ValueTypeIdentifier {
  private final TypeName fieldType;
  private final boolean provided;
  private final Optional<String> providerName;
}
