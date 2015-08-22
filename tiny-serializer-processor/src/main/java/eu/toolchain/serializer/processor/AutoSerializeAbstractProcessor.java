package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.SerializerFramework.TypeMapping;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeAbstractProcessor {
    final Elements elements;
    final FrameworkStatements statements;
    final AutoSerializeUtils utils;

    SerializedType process(final TypeElement element) {
        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String serializerName = statements.serializerName(element);
        final String name = utils.serializedName(element);

        final TypeName elementType = TypeName.get(element.asType());
        final TypeName supertype = TypeName.get(utils.serializerFor(element.asType()));

        final List<SerializedSubType> subtypes = buildSubTypes(element);

        final FieldSpec serializer = FieldSpec.builder(supertype, "serializer", Modifier.FINAL).build();

        final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

        generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        generated.addSuperinterface(supertype);

        generated.addField(serializer);

        generated.addMethod(constructor(elementType, serializer, subtypes));
        generated.addMethod(serializeMethod(elementType, serializer));
        generated.addMethod(deserializeMethod(elementType, serializer));

        final AutoSerialize annotation = utils.requireAnnotation(element, AutoSerialize.class);

        final SerializedFields fields = new SerializedFields(annotation.orderById(), annotation.orderConstructorById());
        return new SerializedType(element, packageName, name, generated.build(), elementType, supertype, fields);
    }

    MethodSpec constructor(TypeName elementType, FieldSpec serializer, final List<SerializedSubType> subtypes) {
        final ClassName list = ClassName.get(List.class);
        final ClassName typeMapping = ClassName.get(TypeMapping.class);
        final ClassName arrayList = ClassName.get(ArrayList.class);

        final ParameterSpec framework = ParameterSpec.builder(SerializerFramework.class, "framework")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();
        b.addModifiers(Modifier.PUBLIC);

        b.addParameter(framework);

        b.addStatement("final $T<$T<? extends $T, $T>> mappings = new $T<>()", list, typeMapping, elementType,
                elementType, arrayList);

        for (final SerializedSubType subtype : subtypes) {
            final ClassName serializerType = statements.serializerClassFor(subtype.type);

            b.addStatement("mappings.add($N.<$T, $T>type($L, $T.class, new $T($N)))", framework, subtype.type,
                    elementType, subtype.id, subtype.type, serializerType, framework);
        }

        b.addStatement("$N = $N.subtypes(mappings)", serializer, framework);
        return b.build();
    }

    MethodSpec serializeMethod(TypeName valueType, FieldSpec serializer) {
        final ParameterSpec buffer = utils.parameter(TypeName.get(SerialWriter.class), "buffer");
        final ParameterSpec value = utils.parameter(valueType, "value");
        return utils.serializeMethod(buffer, value).addStatement("$N.serialize($N, $N)", serializer, buffer, value)
                .build();
    }

    MethodSpec deserializeMethod(TypeName returnType, FieldSpec serializer) {
        final ParameterSpec buffer = utils.parameter(TypeName.get(SerialReader.class), "buffer");
        return utils.deserializeMethod(returnType, buffer)
                .addStatement("return $N.deserialize($N)", serializer, buffer).build();
    }

    List<SerializedSubType> buildSubTypes(Element element) {
        final AutoSerialize.SubTypes annotation = element.getAnnotation(AutoSerialize.SubTypes.class);

        if (annotation == null) {
            return ImmutableList.of();
        }

        final Set<Short> seenIds = new HashSet<>();
        final ImmutableList.Builder<SerializedSubType> subtypes = ImmutableList.builder();

        int offset = 0;
        final ShortIterator index = new ShortIterator();

        for (final AutoSerialize.SubType s : annotation.value()) {
            final ClassName type = utils.pullMirroredClass(s::value);
            final short id = s.id() < 0 ? index.next() : s.id();

            if (!seenIds.add(id)) {
                throw new IllegalStateException(String.format("Conflicting subtype id (%d) defined for definition #%d",
                        id, offset));
            }

            subtypes.add(new SerializedSubType(type, id));
            offset++;
        }

        return subtypes.build();
    }
}