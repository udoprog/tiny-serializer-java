package eu.toolchain.serializer.processor;

import static eu.toolchain.serializer.processor.Exceptions.brokenElement;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.SubTypeMirror;
import eu.toolchain.serializer.processor.value.ValueSubType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeAbstractProcessor {
  final Elements elements;
  final FrameworkStatements statements;
  final AutoSerializeUtils utils;

  public JavaFile process(
    final TypeElement element, final AutoSerializeMirror autoSerialize
  ) {
    final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    final String serializerName = utils.serializerName(element);

    final TypeName elementType = TypeName.get(element.asType());
    final TypeName supertype = TypeName.get(utils.serializerFor(element.asType()));

    final List<ValueSubType> subTypes = subTypes(element, packageName);

    final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

    final AnnotationSpec generatedAnnotation = AnnotationSpec
      .builder(Generated.class)
      .addMember("value", "$S", AutoSerializeProcessor.class.getCanonicalName())
      .build();

    generated.addAnnotation(generatedAnnotation);

    final FieldSpec serializer = FieldSpec.builder(supertype, "serializer", Modifier.FINAL).build();

    generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    generated.addSuperinterface(supertype);

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
    final TypeName elementType, final FieldSpec serializer, final List<ValueSubType> subtypes
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

    for (final ValueSubType subtype : subtypes) {
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

  List<ValueSubType> subTypes(Element element, final String defaultPackageName) {
    return utils.subTypes(element).map(subTypes -> {
      final Set<Short> seenIds = new HashSet<>();

      int offset = 0;
      final ShortIterator index = new ShortIterator();

      final ImmutableList.Builder<ValueSubType> results = ImmutableList.builder();

      for (final SubTypeMirror s : subTypes.getSubTypes()) {
        final DeclaredType type = (DeclaredType) s.getValue().get();
        final short id = s.getId().orElseGet(index::next);

        if (!seenIds.add(id)) {
          throw brokenElement(
            String.format("Conflicting subtype id (%d) defined for definition #%d", id, offset),
            element);
        }

        results.add(new ValueSubType(type, id));
        offset++;
      }

      return results.build();
    }).orElseGet(ImmutableList::of);
  }
}
