package eu.toolchain.serializer.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.toolchain.serializer.processor.field.SubType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbstractClassSpec implements ClassSpec {
  private final AutoSerializeUtils utils;

  final String packageName;
  final String serializerName;
  final TypeName elementType;
  final TypeName superType;
  final List<SubType> subTypes;

  @Override
  public JavaFile toSerializer() {
    final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

    final AnnotationSpec generatedAnnotation = AnnotationSpec
      .builder(Generated.class)
      .addMember("value", "$S", AutoSerializeProcessor.class.getCanonicalName())
      .build();

    generated.addAnnotation(generatedAnnotation);

    final FieldSpec serializer = FieldSpec.builder(superType, "serializer", Modifier.FINAL).build();

    generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    generated.addSuperinterface(superType);

    generated.addField(serializer);

    generated.addMethod(constructor(elementType, serializer, subTypes));
    generated.addMethod(serialize(elementType, serializer));
    generated.addMethod(derialize(elementType, serializer));

    return JavaFile
      .builder(packageName, generated.build())
      .skipJavaLangImports(true)
      .indent("    ")
      .build();
  }

  MethodSpec constructor(
    final TypeName elementType, final FieldSpec serializer, final List<SubType> subtypes
  ) {
    final ClassName list = ClassName.get(List.class);
    final ClassName typeMapping = utils.typeMapping();
    final ClassName arrayList = ClassName.get(ArrayList.class);

    final ParameterSpec framework = ParameterSpec
      .builder(utils.serializerFramework(), "framework")
      .addModifiers(Modifier.FINAL)
      .build();

    final MethodSpec.Builder b = MethodSpec.constructorBuilder();
    b.addModifiers(Modifier.PUBLIC);

    b.addParameter(framework);

    b.addStatement("final $T<$T<? extends $T, $T>> mappings = new $T<>()", list, typeMapping,
      elementType, elementType, arrayList);

    for (final SubType subtype : subtypes) {
      final ClassName serializerType = utils.serializerClassFor(subtype.getType());

      b.addStatement("mappings.add($N.<$T, $T>type($L, $T.class, new $T($N)))", framework,
        subtype.getType(), elementType, subtype.getId(), subtype.getType(), serializerType,
        framework);
    }

    b.addStatement("$N = $N.subtypes(mappings)", serializer, framework);
    return b.build();
  }

  MethodSpec serialize(final TypeName valueType, final FieldSpec serializer) {
    final ParameterSpec buffer = utils.parameter(utils.serialWriter(), "buffer");
    final ParameterSpec value = utils.parameter(valueType, "value");
    return utils
      .serializeMethod(buffer, value)
      .addStatement("$N.serialize($N, $N)", serializer, buffer, value)
      .build();
  }

  MethodSpec derialize(final TypeName returnType, final FieldSpec serializer) {
    final ParameterSpec buffer = utils.parameter(utils.serialReader(), "buffer");
    return utils
      .deserializeMethod(returnType, buffer)
      .addStatement("return $N.deserialize($N)", serializer, buffer)
      .build();
  }
}
