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
    static final ClassName defaultBuilderType = ClassName.get(DefaultBuilderType.class);
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

    public static Optional<SerializedTypeBuilder> build(final AutoSerializeUtils utils, final TypeElement element,
            final String defaultPackageName) throws ElementException {
        final AutoSerialize autoSerialize = utils.requireAnnotation(element, AutoSerialize.class);
        final Optional<AutoSerialize.Builder> direct = Optional.ofNullable(element.getAnnotation(AutoSerialize.Builder.class));

        if (direct.isPresent()) {
            return Optional.of(build(direct.get(), element, utils, defaultPackageName));
        }

        final Optional<AutoSerialize.Builder> nested = providedFrom(autoSerialize.builder());

        if (nested.isPresent()) {
            return Optional.of(build(nested.get(), element, utils, defaultPackageName));
        }

        return Optional.empty();
    }

    static Optional<AutoSerialize.Builder> providedFrom(AutoSerialize.Builder[] builders) {
        if (builders.length == 0) {
            return Optional.empty();
        }

        return Optional.of(builders[0]);
    }

    static SerializedTypeBuilder build(AutoSerialize.Builder builder, final TypeElement element,
            final AutoSerializeUtils utils, final String defaultPackageName) throws ElementException {
        final Optional<ClassName> optionalBuilderType = utils.pullMirroredClass(builder::type, defaultPackageName);

        if (!optionalBuilderType.isPresent()) {
            throw new ElementException("@AutoSerialize.Builder(type=<class>) does not reference a valid Class", element);
        }

        final ClassName builderType = optionalBuilderType.get();
        final boolean useConstructor = shouldUseConstructor(builderType, builder);
        return new SerializedTypeBuilder(builderType, useConstructor, builder.useSetter(), builder.methodName());
    }

    private static boolean shouldUseConstructor(ClassName builderType, AutoSerialize.Builder builder) {
        // use explicitly ask to use method.
        if (builder.useMethod()) {
            return false;
        }

        // use explicitly ask to use constructor.
        if (builder.useConstructor()) {
            return true;
        }

        // by policy, if a type is specified, the constructor should be used.
        if (!builderType.equals(defaultBuilderType)) {
            return true;
        }

        return false;
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
        if (!builderType.equals(defaultBuilderType)) {
            return builderType;
        }

        return returnType.nestedClass("Builder");
    }

    TypeName getBuilderTypeForMethod(ClassName returnType) {
        if (!builderType.equals(defaultBuilderType)) {
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