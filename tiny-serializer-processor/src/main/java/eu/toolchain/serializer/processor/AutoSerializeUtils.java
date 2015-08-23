package eu.toolchain.serializer.processor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.processor.annotation.AnnotationValues;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.BuilderMirror;
import eu.toolchain.serializer.processor.annotation.FieldMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeUtils {
    final Types types;
    final Elements elements;

    public MethodSpec.Builder deserializeMethod(final TypeName returnType, final ParameterSpec buffer) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder("deserialize");

        b.addModifiers(Modifier.PUBLIC);
        b.returns(returnType);
        b.addAnnotation(Override.class);
        b.addParameter(buffer);
        b.addException(IOException.class);

        return b;
    }

    public MethodSpec.Builder serializeMethod(final ParameterSpec buffer, final ParameterSpec value) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder("serialize");

        b.addModifiers(Modifier.PUBLIC);
        b.returns(TypeName.VOID);
        b.addAnnotation(Override.class);
        b.addParameter(buffer);
        b.addParameter(value);
        b.addException(IOException.class);

        return b;
    }

    public ParameterSpec parameter(TypeName type, String name) {
        return ParameterSpec.builder(type, name).addModifiers(Modifier.FINAL).build();
    }

    public String serializedName(Element element) {
        final AutoSerialize annotation = requireAnnotation(element, AutoSerialize.class);

        if (!"".equals(annotation.name())) {
            return annotation.name();
        }

        return element.getSimpleName().toString();
    }

    public <T extends Annotation> T requireAnnotation(Element element, Class<T> annotationType) {
        final T annotation = element.getAnnotation(annotationType);

        if (annotation == null) {
            throw new IllegalArgumentException(String.format("Type not annotated with @%s (%s)", annotationType.getSimpleName(), element));
        }

        return annotation;
    }

    public TypeMirror serializerFor(TypeMirror type) {
        final TypeElement serializer = elements.getTypeElement(Serializer.class.getCanonicalName());
        return types.getDeclaredType(serializer, boxedIfNeeded(type));
    }

    TypeMirror boxedIfNeeded(TypeMirror type) {
        if (type instanceof PrimitiveType) {
            return types.boxedClass((PrimitiveType) type).asType();
        }

        return type;
    }

    public Optional<ClassName> pullMirroredClass(Supplier<Class<?>> supplier, String defaultPackageName) {
        try {
            return Optional.of(ClassName.get(supplier.get()));
        } catch (final MirroredTypeException e) {
            return buildClassName(e, e.getTypeMirror(), defaultPackageName);
        }
    }

    private Optional<ClassName> buildClassName(final MirroredTypeException e, final TypeMirror type, final String defaultPackageName) {
        if (type instanceof ErrorType) {
            return Optional.empty();
        }

        return Optional.of((ClassName)TypeName.get(type));
    }

    /**
     * Indicates if this type uses builders or not.
     *
     * @param element
     * @return
     */
    public boolean useBuilder(TypeElement element) {
        final AutoSerialize autoSerialize = requireAnnotation(element, AutoSerialize.class);

        if (element.getAnnotation(AutoSerialize.Builder.class) != null) {
            return true;
        }

        return autoSerialize.builder().length > 0;
    }

    /**
     * Re-fetch the given element from the environment.
     *
     * This might be necessary to update type information which was not available on previous rounds.
     *
     * @param element Element to fetch.
     * @return A refreshed version of the specified element from the environment.
     */
    public TypeElement refetch(TypeElement element) {
        return elements.getTypeElement(element.getQualifiedName());
    }

    public List<AnnotationMirror> getAnnotations(Element element, Class<? extends Annotation> annotationType) {
        final String lookFor = annotationType.getCanonicalName();

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

        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : elements.getElementValuesWithDefaults(a).entrySet()) {
            builder.put(e.getKey().getSimpleName().toString(), e.getValue());
        }

        return new AnnotationValues(element, a, builder.build());
    }

    public <T extends Annotation> Optional<AnnotationMirror> annotation(Element element, Class<T> annotationType) {
        for (final AnnotationMirror a : getAnnotations(element, annotationType)) {
            return Optional.of(a);
        }

        return Optional.empty();
    }

    public Optional<Unverified<AutoSerializeMirror>> autoSerialize(Element element) {
        return annotation(element, AutoSerialize.class).map((a) -> AutoSerializeMirror.getFor(this, element, a));
    }

    public Optional<FieldMirror> field(Element element) {
        return annotation(element, AutoSerialize.Field.class).map((a) -> FieldMirror.getFor(this, element, a));
    }

    public Optional<Unverified<BuilderMirror>> builder(Element element) {
        return annotation(element, AutoSerialize.Builder.class).map((a) -> BuilderMirror.getFor(this, element, a));
    }
}