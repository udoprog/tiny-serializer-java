package eu.toolchain.serializer.processor;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.SerializerFramework.TypeMapping;
import eu.toolchain.serializer.processor.annotation.AnnotationValues;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.BuilderMirror;
import eu.toolchain.serializer.processor.annotation.FieldMirror;
import eu.toolchain.serializer.processor.annotation.IgnoreMirror;
import eu.toolchain.serializer.processor.annotation.ProvidedMirror;
import eu.toolchain.serializer.processor.annotation.SubTypeMirror;
import eu.toolchain.serializer.processor.annotation.SubTypesMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeUtils {
  public static final String SERIALIZER_NAME_FORMAT = "%s_Serializer";
  public static final Joiner underscoreJoiner = Joiner.on('_');

  public static final String SERIALIZER = Serializer.class.getCanonicalName();
  public static final String AUTOSERIALIZE = AutoSerialize.class.getCanonicalName();
  public static final String AUTOSERIALIZE_IGNORE = AutoSerialize.Ignore.class.getCanonicalName();
  public static final String AUTOSERIALIZE_PROVIDED =
    AutoSerialize.Provided.class.getCanonicalName();
  public static final String AUTOSERIALIZE_BUILDER = AutoSerialize.Builder.class.getCanonicalName();
  public static final String AUTOSERIALIZE_SUBTYPE = AutoSerialize.SubType.class.getCanonicalName();
  public static final String AUTOSERIALIZE_SUBTYPES =
    AutoSerialize.SubTypes.class.getCanonicalName();
  public static final String AUTOSERIALIZE_FIELD = AutoSerialize.Field.class.getCanonicalName();

  public static final String SERIALIZER_FRAMEWORK = SerializerFramework.class.getCanonicalName();
  public static final String SERIAL_READER = SerialReader.class.getCanonicalName();
  public static final String SERIAL_WRITER = SerialWriter.class.getCanonicalName();
  public static final String TYPE_MAPPING = TypeMapping.class.getCanonicalName();
  public static final String DEFAULT_BUILDER_TYPE = Void.class.getCanonicalName();

  public static final String OPTIONAL = Optional.class.getCanonicalName();

  public static final String IO_EXCEPTION = IOException.class.getCanonicalName();

  final Types types;
  final Elements elements;

  final TypeElement serializer;
  final TypeElement autoSerializeType;

  public AutoSerializeUtils(Types types, Elements elements) {
    this.types = types;
    this.elements = elements;
    this.serializer = elements.getTypeElement(SERIALIZER);
    this.autoSerializeType = elements.getTypeElement(AUTOSERIALIZE);
  }

  public MethodSpec.Builder deserializeMethod(
    final TypeName returnType, final ParameterSpec buffer
  ) {
    final MethodSpec.Builder b = MethodSpec.methodBuilder("deserialize");

    b.addModifiers(Modifier.PUBLIC);
    b.returns(returnType);
    b.addAnnotation(Override.class);
    b.addParameter(buffer);
    b.addException(IOException.class);

    return b;
  }

  public MethodSpec.Builder serializeMethod(
    final ParameterSpec buffer, final ParameterSpec value
  ) {
    final MethodSpec.Builder b = MethodSpec.methodBuilder("serialize");

    b.addModifiers(Modifier.PUBLIC);
    b.returns(TypeName.VOID);
    b.addAnnotation(Override.class);
    b.addParameter(buffer);
    b.addParameter(value);
    b.addException(IOException.class);

    return b;
  }

  public ParameterSpec parameter(final TypeName type, final String name) {
    return ParameterSpec.builder(type, name).addModifiers(Modifier.FINAL).build();
  }

  public TypeMirror serializerFor(TypeMirror type) {
    return types.getDeclaredType(serializer, boxedIfNeeded(type));
  }

  public boolean isPrimitive(TypeMirror type) {
    return type instanceof PrimitiveType;
  }

  public TypeMirror boxedIfNeeded(TypeMirror type) {
    if (type instanceof PrimitiveType) {
      return types.boxedClass((PrimitiveType) type).asType();
    }

    return type;
  }

  public Optional<ClassName> pullMirroredClass(
    Supplier<Class<?>> supplier, String defaultPackageName
  ) {
    try {
      return Optional.of(ClassName.get(supplier.get()));
    } catch (final MirroredTypeException e) {
      return buildClassName(e, e.getTypeMirror(), defaultPackageName);
    }
  }

  private Optional<ClassName> buildClassName(
    final MirroredTypeException e, final TypeMirror type, final String defaultPackageName
  ) {
    if (type instanceof ErrorType) {
      return Optional.empty();
    }

    return Optional.of((ClassName) TypeName.get(type));
  }

  /**
   * Re-fetch the given element from the environment.
   * <p>
   * This might be necessary to update type information which was not available on previous
   * rounds.
   *
   * @param element Element to fetch.
   * @return A refreshed version of the specified element from the environment.
   */
  public TypeElement refetch(TypeElement element) {
    return elements.getTypeElement(element.getQualifiedName());
  }

  public List<AnnotationMirror> getAnnotations(Element element, String lookFor) {
    final ImmutableList.Builder<AnnotationMirror> results = ImmutableList.builder();

    for (final AnnotationMirror annotation : element.getAnnotationMirrors()) {
      if (!annotation.getAnnotationType().toString().equals(lookFor)) {
        continue;
      }

      results.add(annotation);
    }

    return results.build();
  }

  public AnnotationValues getElementValuesWithDefaults(Element element, AnnotationMirror a) {
    final ImmutableMap.Builder<String, AnnotationValue> builder = ImmutableMap.builder();

    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : elements
      .getElementValuesWithDefaults(a)
      .entrySet()) {
      builder.put(e.getKey().getSimpleName().toString(), e.getValue());
    }

    return new AnnotationValues(element, a, builder.build());
  }

  public <T extends Annotation> Optional<AnnotationMirror> annotation(
    Element element, String annotationType
  ) {
    for (final AnnotationMirror a : getAnnotations(element, annotationType)) {
      return Optional.of(a);
    }

    return Optional.empty();
  }

  public Optional<AutoSerializeMirror> autoSerialize(Element element) {
    return annotation(element, AUTOSERIALIZE).map(
      (a) -> AutoSerializeMirror.getFor(this, element, a));
  }

  public Optional<SubTypeMirror> subType(Element element) {
    return annotation(element, AUTOSERIALIZE_SUBTYPE).map(
      (a) -> SubTypeMirror.getFor(this, element, a));
  }

  public Optional<SubTypesMirror> subTypes(Element element) {
    return annotation(element, AUTOSERIALIZE_SUBTYPES).map(
      (a) -> SubTypesMirror.getFor(this, element, a));
  }

  public Optional<FieldMirror> field(Element element) {
    return annotation(element, AUTOSERIALIZE_FIELD).map(
      (a) -> FieldMirror.getFor(this, element, a));
  }

  public Optional<BuilderMirror> builder(Element element) {
    return annotation(element, AUTOSERIALIZE_BUILDER).map(
      (a) -> BuilderMirror.getFor(this, element, a));
  }

  public Optional<IgnoreMirror> ignore(Element element) {
    return annotation(element, AUTOSERIALIZE_IGNORE).map(
      (a) -> IgnoreMirror.getFor(this, element, a));
  }

  public Optional<ProvidedMirror> provided(Element element) {
    return annotation(element, AUTOSERIALIZE_PROVIDED).map(
      (a) -> ProvidedMirror.getFor(this, element, a));
  }

  public TypeElement autoSerializeType() {
    return autoSerializeType;
  }

  public ClassName serializerFramework() {
    return ClassName.get(elements.getTypeElement(SERIALIZER_FRAMEWORK));
  }

  public ClassName serialReader() {
    return ClassName.get(elements.getTypeElement(SERIAL_READER));
  }

  public ClassName serialWriter() {
    return ClassName.get(elements.getTypeElement(SERIAL_WRITER));
  }

  public ClassName typeMapping() {
    return ClassName.get(elements.getTypeElement(TYPE_MAPPING));
  }

  public ClassName optional() {
    return ClassName.get(elements.getTypeElement(OPTIONAL));
  }

  public ClassName ioException() {
    return ClassName.get(elements.getTypeElement(IO_EXCEPTION));
  }

  public boolean isOptional(TypeMirror valueType) {
    if (!(valueType instanceof DeclaredType)) {
      return false;
    }

    final DeclaredType d = (DeclaredType) valueType;
    final TypeElement t = (TypeElement) d.asElement();
    return t.getQualifiedName().toString().equals(Optional.class.getCanonicalName());
  }

  public String initLiteral(TypeMirror type) {
    if (!(type instanceof PrimitiveType)) {
      return "null";
    }

    final PrimitiveType p = (PrimitiveType) type;
    switch (p.getKind()) {
      case BOOLEAN:
        return "false";
      case SHORT:
        return "0";
      case INT:
        return "0";
      case LONG:
        return "0L";
      case FLOAT:
        return "0f";
      case DOUBLE:
        return "0d";
      case BYTE:
        return "0";
      case CHAR:
        return "'\0'";
      default:
        throw new IllegalArgumentException("Unsupported primitive: " + type.toString());
    }
  }

  public String serializerName(final Element root) {
    final ImmutableList.Builder<String> parts = ImmutableList.builder();

    Element element = root;

    do {
      if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE) {
        throw new IllegalArgumentException(
          String.format("Element is not interface or class (%s)", element));
      }

      if (element.getEnclosingElement().getKind() == ElementKind.CLASS &&
        !element.getModifiers().contains(Modifier.STATIC)) {
        throw new IllegalArgumentException(
          String.format("Nested element must be static (%s)", element));
      }

      parts.add(element.getSimpleName().toString());
      element = element.getEnclosingElement();
    } while (element.getKind() != ElementKind.PACKAGE);

    return String.format(SERIALIZER_NAME_FORMAT, underscoreJoiner.join(parts.build().reverse()));
  }

  public ClassName serializerClassFor(final DeclaredType type) {
    final String pkg = elements.getPackageOf(type.asElement()).getQualifiedName().toString();
    final String name = serializerName(type.asElement());
    return ClassName.get(pkg, name);
  }
}
