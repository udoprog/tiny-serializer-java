package eu.toolchain.serializer.processor.value;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.Optional;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class ValueType {
  private final ValueTypeIdentifier identifier;
  private final TypeMirror typeMirror;
  private final TypeName typeName;
  private final FieldSpec fieldSpec;
  private final Optional<ParameterSpec> providedParameterSpec;
  private final Optional<Integer> id;
  private final boolean optional;
}
