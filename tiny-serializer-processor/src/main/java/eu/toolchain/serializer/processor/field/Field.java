package eu.toolchain.serializer.processor.field;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import java.util.List;
import java.util.Optional;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class Field {
  /**
   * Original name of the field.
   */
  private final String originalName;
  /**
   * Configured provider name.
   */
  private final Optional<String> providerName;
  /**
   * If the serializer for this field is provided or not.
   */
  private final boolean provided;
  /**
   * If the value for this field is provided.
   */
  private final boolean external;
  /**
   * Type of the field.
   */
  private final TypeMirror type;
  /**
   * The field specification.
   */
  private final FieldSpec field;
  /**
   * If it is a provided variable, this is the parameter spec.
   */
  private final Optional<ParameterSpec> providedParameter;
  /**
   * If this field is optional or not.
   */
  private final boolean optional;
  /**
   * Fields provided to subtypes.
   */
  private final List<Field> subFields;

  public Field withProvidedParameter(final Optional<ParameterSpec> providedParameter) {
    return new Field(originalName, providerName, provided, external, type, field, providedParameter,
      optional, subFields);
  }
}
