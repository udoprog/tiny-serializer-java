package eu.toolchain.serializer.processor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.Serializer;
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

    public ClassName pullMirroredClass(Supplier<Class<?>> supplier) {
        try {
            return ClassName.get(supplier.get());
        } catch (final MirroredTypeException e) {
            return (ClassName) TypeName.get(e.getTypeMirror());
        }
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
}