package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.SerializerFramework.TypeMapping;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.SubTypeMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoSerializeAbstractProcessor {
    final Elements elements;
    final FrameworkStatements statements;
    final AutoSerializeUtils utils;

    public Unverified<SerializedType> process(final TypeElement element, final AutoSerializeMirror autoSerialize) {
        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String serializerName = statements.serializerName(element);

        final TypeName elementType = TypeName.get(element.asType());
        final TypeName supertype = TypeName.get(utils.serializerFor(element.asType()));

        return Unverified.combine(buildSubTypes(element, packageName)).<SerializedType> map((subtypes) -> 
            () -> {
                final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

                generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
                generated.addSuperinterface(supertype);

                final FieldSpec serializer = FieldSpec.builder(supertype, "serializer", Modifier.FINAL).build();
                generated.addField(serializer);

                generated.addMethod(constructor(elementType, serializer, subtypes));
                generated.addMethod(serializeMethod(elementType, serializer));
                generated.addMethod(deserializeMethod(elementType, serializer));

                return JavaFile.builder(packageName, generated.build()).skipJavaLangImports(true).indent("    ")
                        .build();
            }
        );
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

    Unverified<List<Unverified<SerializedSubType>>> buildSubTypes(Element element, final String defaultPackageName) {
        return utils.subTypes(element).map((unverifiedSubTypes) -> {
            return unverifiedSubTypes.map((List<SubTypeMirror> subTypes) -> {
                final Set<Short> seenIds = new HashSet<>();

                int offset = 0;
                final ShortIterator index = new ShortIterator();

                final ImmutableList.Builder<Unverified<SerializedSubType>> subtypes = ImmutableList.builder();

                for (final SubTypeMirror s : subTypes.getSubTypes()) {
                    final ClassName type = (ClassName)TypeName.get(s.getValue().get());

                    final short id = s.getId().filter((i) -> i < Short.MAX_VALUE).map(Integer::shortValue)
                            .orElseGet(index::next);
g
                    if (!seenIds.add(id)) {
                        subtypes.add(Unverified.brokenElement(String.format("Conflicting subtype id (%d) defined for definition #%d", id, offset), element));
                        continue;
                    }

                    subtypes.add(Unverified.verified(new SerializedSubType(type, id)));
                    offset++;
                }

                return (List<Unverified<SerializedSubType>>)subtypes.build();
            });
        }).orElse(Unverified.verified(ImmutableList.of()));
    }
}