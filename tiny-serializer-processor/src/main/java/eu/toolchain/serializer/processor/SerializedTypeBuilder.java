package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.TypeElement;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.BuilderMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

/**
 * Describes how a strategy for how to setup a specified builder.
 *
 * @author udoprog
 */
@Data
public class SerializedTypeBuilder {
    static final Joiner emptyJoiner = Joiner.on("");

    final BuilderMirror builder;

    /**
     * If {@code true}, indicates that the constructor of the builder should be used when instantiating it.
     */
    final boolean useConstructor;

    /**
     * If {@code true}, indicates that builder methods are setters instead of using a one-to-one mapping of field names.
     */
    final boolean useSetter;

    /**
     * Contains the name of the method to use, unless {@link #useConstructor} is {@code true}.
     */
    final String method;

    public static Unverified<Optional<SerializedTypeBuilder>> build(final AutoSerializeUtils utils,
            final TypeElement element, final AutoSerializeMirror autoSerialize) {
        return utils.builder(element).map((unverifiedDirect) -> {
            return unverifiedDirect.map((direct) -> {
                return Optional.of(build(direct, element, utils));
            });
        }).orElseGet(() -> {
            return Unverified.verified(autoSerialize.getBuilder().stream().findFirst().map((nested) -> {
                return Optional.of(build(nested, element, utils));
            }).orElse(Optional.empty()));
        });
    }

    static SerializedTypeBuilder build(final BuilderMirror builder, final TypeElement element,
            final AutoSerializeUtils utils) {
        final boolean useConstructor = builder.shouldUseConstructor();
        return new SerializedTypeBuilder(builder, useConstructor, builder.isUseSetter(), builder.getMethodName());
    }

    public void writeTo(ClassName returnType, MethodSpec.Builder b, List<SerializedField> variables) {
        final ImmutableList.Builder<String> builders = ImmutableList.builder();
        final ImmutableList.Builder<Object> parameters = ImmutableList.builder();

        final TypeName builderType;
        final String builderStatement;

        if (useConstructor) {
            builderType = getBuilderTypeForConstructor(returnType);
            builderStatement = "new $T()";
        } else {
            builderType = getBuilderTypeForMethod(returnType);
            builderStatement = String.format("$T.%s()", method);
        }

        parameters.add(builderType);

        for (final SerializedField f : variables) {
            final String setter = builderSetter(f);
            builders.add(String.format(".%s($L)", setter));
            parameters.add(f.getVariableName());
        }

        b.addStatement(String.format("return %s%s.build()", builderStatement, emptyJoiner.join(builders.build())),
                parameters.build().toArray());
    }

    TypeName getBuilderTypeForConstructor(ClassName returnType) {
        return builder.getType().map((t) -> TypeName.get(t.get())).orElse(returnType.nestedClass("Builder"));
    }

    TypeName getBuilderTypeForMethod(ClassName returnType) {
        return builder.getType().map((t) -> TypeName.get(t.get())).orElse(returnType);
    }

    String builderSetter(final SerializedField f) {
        if (useSetter) {
            return "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, f.getFieldName());
        }

        return f.getFieldName();
    }
}