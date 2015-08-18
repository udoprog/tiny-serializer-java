package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public class FrameworkStatements {
    static final Map<TypeName, String> frameworkSupported = new HashMap<>();

    static {
        frameworkSupported.put(ClassName.get(String.class), "$N.string()");
        frameworkSupported.put(ClassName.get(Short.class), "$N.shortNumber()");
        frameworkSupported.put(ClassName.get(Integer.class), "$N.integer()");
        frameworkSupported.put(ClassName.get(Long.class), "$N.longNumber()");
        frameworkSupported.put(ClassName.get(Float.class), "$N.floatNumber()");
        frameworkSupported.put(ClassName.get(Double.class), "$N.doubleNumber()");
        frameworkSupported.put(ClassName.get(Boolean.class), "$N.bool()");
        frameworkSupported.put(ClassName.get(UUID.class), "$N.uuid()");
        frameworkSupported.put(TypeName.get(byte[].class), "$N.byteArray()");
        frameworkSupported.put(TypeName.get(char[].class), "$N.charArray()");
    }

    static ClassName listClass = ClassName.get(List.class);
    static ClassName mapClass = ClassName.get(Map.class);
    static ClassName setClass = ClassName.get(Set.class);

    public static FrameworkStatement resolveStatement(final TypeName type, final Object framework) {
        final String statement = frameworkSupported.get(type);

        if (statement != null) {
            return new FrameworkStatement() {
                @Override
                public void writeTo(FrameworkMethodBuilder builder) {
                    builder.assign(statement, ImmutableList.of(framework));
                }
            };
        }

        if (type instanceof ClassName) {
            return resolveCustomSerializer((ClassName) type, framework);
        }

        if (type instanceof ParameterizedTypeName) {
            return resolveParameterizedType((ParameterizedTypeName) type, framework);
        }

        throw new IllegalArgumentException("Unsupported type: " + type.toString());
    }

    static FrameworkStatement resolveList(final FrameworkStatement nested, final Object framework) {
        return new FrameworkStatement() {
            @Override
            public void writeTo(final FrameworkMethodBuilder builder) {
                nested.writeTo(new FrameworkMethodBuilder() {
                    @Override
                    public void assign(String statement, List<Object> arguments) {
                        builder.assign(String.format("$N.list(%s)", statement),
                                ImmutableList.builder().add(framework).addAll(arguments).build());
                    }
                });
            }
        };
    }

    static Joiner argumentJoiner = Joiner.on(", ");

    static FrameworkStatement resolveGeneric(final String statementBase, final List<Object> argumentsBase,
            final List<FrameworkStatement> statements) {
        final List<String> typeStatements = new ArrayList<>();

        final ImmutableList.Builder<Object> outerArguments = ImmutableList.builder();

        outerArguments.addAll(argumentsBase);

        for (final FrameworkStatement a : statements) {
            a.writeTo(new FrameworkMethodBuilder() {
                @Override
                public void assign(String statement, List<Object> arguments) {
                    typeStatements.add(statement);
                    outerArguments.addAll(arguments);
                }
            });
        }

        final String statement = String.format("%s(%s)", statementBase, argumentJoiner.join(typeStatements));
        final List<Object> arguments = outerArguments.build();

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                builder.assign(statement, arguments);
            }
        };
    }

    static FrameworkStatement resolveCustomSerializer(final ClassName type, final Object framework) {
        final String statement = "new $T($N)";
        final List<Object> arguments = ImmutableList.of(AutoSerializerProcessor.serializerClassFor(type), framework);

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                builder.assign(statement, arguments);
            }
        };
    }

    static FrameworkStatement resolveParameterizedType(ParameterizedTypeName type, Object framework) {
        if (type.rawType.equals(listClass)) {
            final Iterator<TypeName> types = type.typeArguments.iterator();
            final ImmutableList.Builder<FrameworkStatement> statements = ImmutableList.builder();
            statements.add(resolveStatement(types.next(), framework));
            return resolveGeneric("$N.list", ImmutableList.of(framework), statements.build());
        }

        if (type.rawType.equals(mapClass)) {
            final Iterator<TypeName> types = type.typeArguments.iterator();
            final ImmutableList.Builder<FrameworkStatement> statements = ImmutableList.builder();
            statements.add(resolveStatement(types.next(), framework));
            statements.add(resolveStatement(types.next(), framework));
            return resolveGeneric("$N.map", ImmutableList.of(framework), statements.build());
        }

        if (type.rawType.equals(setClass)) {
            final Iterator<TypeName> types = type.typeArguments.iterator();
            final ImmutableList.Builder<FrameworkStatement> statements = ImmutableList.builder();
            statements.add(resolveStatement(types.next(), framework));
            return resolveGeneric("$N.set", ImmutableList.of(framework), statements.build());
        }

        throw new IllegalArgumentException("Unsupported type: " + type.toString());
    }
}