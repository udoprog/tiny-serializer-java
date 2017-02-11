package eu.toolchain.serializer.processor.field;

import com.squareup.javapoet.ParameterSpec;
import lombok.Data;

@Data
public class ProvidedField {
  private final ParameterSpec parameterSpec;
}
