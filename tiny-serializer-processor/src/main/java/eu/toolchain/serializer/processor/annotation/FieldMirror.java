package eu.toolchain.serializer.processor.annotation;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.Naming;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class FieldMirror {
  private final Optional<String> name;
  private final Optional<String> fieldName;
  private final Optional<String> accessor;
  private final boolean useGetter;
  private final boolean provided;
  private final boolean external;
  private final Optional<String> providerName;

  public static FieldMirror defaultInstance(final boolean defaultUseGetter) {
    return new FieldMirror(Optional.empty(), Optional.empty(), Optional.empty(), defaultUseGetter,
      false, false, Optional.empty());
  }

  public Optional<ParameterSpec> buildProvidedParameter(
    final AutoSerializeUtils utils, final TypeMirror fieldType, final Naming providerNaming,
    final String variableName
  ) {
    if (provided) {
      final TypeName serializerType = TypeName.get(utils.serializerFor(fieldType));
      return Optional.of(
        buildProvidedSpec(serializerType, providerName, fieldType, providerNaming));
    }

    if (external) {
      return Optional.of(
        ParameterSpec.builder(TypeName.get(fieldType), variableName, Modifier.FINAL).build());
    }

    return Optional.empty();
  }

  public static ParameterSpec buildProvidedSpec(
    final TypeName serializerType, final Optional<String> providerName, final TypeMirror fieldType,
    final Naming naming
  ) {
    final String uniqueProviderName;

    if (providerName.isPresent()) {
      uniqueProviderName = naming.forName(providerName.get());
    } else {
      uniqueProviderName = naming.forType(TypeName.get(fieldType), false);
    }

    return ParameterSpec.builder(serializerType, uniqueProviderName, Modifier.FINAL).build();
  }

  public FieldSpec buildField(
    final AutoSerializeUtils utils, final TypeMirror fieldType, final Naming fieldNaming,
    final String variableName
  ) {
    if (external) {
      return FieldSpec
        .builder(TypeName.get(fieldType), variableName)
        .addModifiers(Modifier.FINAL)
        .build();
    }

    final String typeFieldName;

    if (provided) {
      if (providerName.isPresent()) {
        typeFieldName = fieldNaming.forName(providerName.get());
      } else {
        typeFieldName = fieldNaming.forType(TypeName.get(fieldType), provided);
      }
    } else {
      typeFieldName = fieldNaming.forType(TypeName.get(fieldType), provided);
    }

    final TypeName serializerType = TypeName.get(utils.serializerFor(fieldType));
    return FieldSpec.builder(serializerType, typeFieldName).addModifiers(Modifier.FINAL).build();
  }
}
