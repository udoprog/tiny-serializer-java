package eu.toolchain.serializer.processor;

import java.util.List;

import javax.lang.model.element.TypeElement;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.DefaultBuilderType;
import lombok.Data;

/**
 * Describes how a strategy for how to setup a specified builder.
 *
 * @author udoprog
 */
@Data
public class SerializedTypeBuilder {
    static final ClassName defaultBuilder = ClassName.get(DefaultBuilderType.class);
    static final Joiner emptyJoiner = Joiner.on("");

    final ClassName builderType;

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

    public static Optional<SerializedTypeBuilder> build(final AutoSerializeUtils utils, final TypeElement element) {
        final AutoSerialize autoSerialize = utils.requireAnnotation(element, AutoSerialize.class);
        final AutoSerialize.Builder direct = element.getAnnotation(AutoSerialize.Builder.class);
        return Optional.fromNullable(direct).or(providedFrom(autoSerialize.builder())).transform(build(utils));
    }

    static Optional<AutoSerialize.Builder> providedFrom(AutoSerialize.Builder[] builders) {
        if (builders.length == 0) {
            return Optional.absent();
        }

        return Optional.of(builders[0]);
    }

    static Function<AutoSerialize.Builder, SerializedTypeBuilder> build(final AutoSerializeUtils utils) {
        return (builder) -> {
            final ClassName builderType = utils.pullMirroredClass(builder::type);
            return new SerializedTypeBuilder(builderType, builder.useConstructor(), builder.useSetter(), builder.useMethod());
        };
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
        if (!builderType.equals(defaultBuilder)) {
            return builderType;
        }

        return returnType.nestedClass("Builder");
    }

    TypeName getBuilderTypeForMethod(ClassName returnType) {
        if (!builderType.equals(defaultBuilder)) {
            return builderType;
        }

        return returnType;
    }

    String builderSetter(final SerializedField f) {
        if (useSetter) {
            return "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, f.getFieldName());
        }

        return f.getFieldName();
    }
}