package eu.toolchain.serializer.processor.field;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.Optional;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class FieldType {
  private final FieldTypeId identifier;
  private final TypeMirror typeMirror;
  private final TypeName typeName;
  private final FieldSpec fieldSpec;
  private final Optional<ParameterSpec> providedParameterSpec;
  private final boolean optional;
  private final Optional<Integer> id;
}
