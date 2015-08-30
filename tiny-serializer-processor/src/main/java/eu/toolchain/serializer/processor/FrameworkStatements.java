package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FrameworkStatements {
    public static final String SERIALIZER_NAME_FORMAT = "%s_Serializer";
    public static final Joiner underscoreJoiner = Joiner.on('_');

    static final Map<TypeName, String> direct = new HashMap<>();

    static {
        direct.put(ClassName.get(String.class), "$N.string()");
        direct.put(ClassName.get(Short.class), "$N.shortNumber()");
        direct.put(ClassName.get(Integer.class), "$N.integer()");
        direct.put(ClassName.get(Long.class), "$N.longNumber()");
        direct.put(ClassName.get(Float.class), "$N.floatNumber()");
        direct.put(ClassName.get(Double.class), "$N.doubleNumber()");
        direct.put(ClassName.get(Boolean.class), "$N.bool()");
        direct.put(ClassName.get(UUID.class), "$N.uuid()");
        direct.put(TypeName.get(boolean[].class), "$N.booleanArray()");
        direct.put(TypeName.get(short[].class), "$N.shortArray()");
        direct.put(TypeName.get(int[].class), "$N.intArray()");
        direct.put(TypeName.get(long[].class), "$N.longArray()");
        direct.put(TypeName.get(float[].class), "$N.floatArray()");
        direct.put(TypeName.get(double[].class), "$N.doubleArray()");
        direct.put(TypeName.get(byte[].class), "$N.byteArray()");
        direct.put(TypeName.get(char[].class), "$N.charArray()");
    }

    static final List<ParameterizedTypeStatement> parameterized = new ArrayList<>();

    static {
        parameterized.add(new ParameterizedTypeStatement(ClassName.get(List.class), "$N.list", 1));
        parameterized.add(new ParameterizedTypeStatement(ClassName.get(Map.class), "$N.map", 2));
        parameterized.add(new ParameterizedTypeStatement(ClassName.get(SortedMap.class), "$N.sortedMap", 2));
        parameterized.add(new ParameterizedTypeStatement(ClassName.get(Set.class), "$N.set", 1));
        parameterized.add(new ParameterizedTypeStatement(ClassName.get(SortedSet.class), "$N.sortedSet", 1));
        parameterized.add(new ParameterizedTypeStatement(ClassName.get(Optional.class), "$N.optional", 1));
    }

    private final AutoSerializeUtils utils;

    public String serializerName(final Element root) {
        final ImmutableList.Builder<String> parts = ImmutableList.builder();

        Element element = root;

        do {
            if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE) {
                throw new IllegalArgumentException(String.format("Element is not interface or class (%s)", element));
            }

            if (element.getEnclosingElement().getKind() == ElementKind.CLASS && !element.getModifiers().contains(Modifier.STATIC)) {
                throw new IllegalArgumentException(String.format("Nested element must be static (%s)", element));
            }

            parts.add(element.getSimpleName().toString());
            element = element.getEnclosingElement();
        } while (element.getKind() != ElementKind.PACKAGE);

        return String.format(SERIALIZER_NAME_FORMAT, underscoreJoiner.join(parts.build().reverse()));
    }

    public ClassName serializerClassFor(ClassName type) {
        return ClassName.get(type.packageName(), String.format(SERIALIZER_NAME_FORMAT, type.simpleName()));
    }

    public FrameworkStatement resolveStatement(TypeMirror type, final Object framework) {
        final String statement = direct.get(TypeName.get(type));

        if (statement != null) {
            return new FrameworkStatement() {
                @Override
                public void writeTo(FrameworkMethodBuilder builder) {
                    builder.assign(statement, ImmutableList.of(framework));
                }
            };
        }

        if (type instanceof ArrayType) {
            final ArrayType a = (ArrayType) type;
            return resolveArrayType(a, framework);
        }

        final DeclaredType d = (DeclaredType) type;

        if (d.asElement().getKind() == ElementKind.ENUM) {
            return resolveEnum((TypeElement) d.asElement(), framework);
        }

        if (d.getTypeArguments().isEmpty()) {
            return resolveCustomSerializer(d, framework);
        }

        return resolveParameterizedType(d, framework);
    }

    private FrameworkStatement resolveArrayType(ArrayType a, Object framework) {
        final TypeMirror componentType = a.getComponentType();

        if (utils.isPrimitive(componentType)) {
            throw new IllegalArgumentException("Cannot serialize array with a primitive component type: " + a);
        }

        final FrameworkStatement component = resolveStatement(utils.boxedIfNeeded(componentType), framework);

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                component.writeTo((cs, ca) -> {
                    final List<Object> arguments = new ArrayList<>();

                    arguments.add(framework);
                    arguments.addAll(ca);
                    arguments.add(TypeName.get(componentType));

                    builder.assign(String.format("$N.array(%s, (s) -> new $T[s])", cs), arguments);
                });
            }
        };
    }

    private FrameworkStatement resolveEnum(final TypeElement element, final Object framework) {
        final ClassName enumType = ClassName.get(element);

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                builder.assign("$N.forEnum($T.values())", ImmutableList.of(framework, enumType));
            }
        };
    }

    static Joiner argumentJoiner = Joiner.on(", ");

    FrameworkStatement resolveGeneric(final String statementBase, final List<Object> argumentsBase,
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

    FrameworkStatement resolveCustomSerializer(final DeclaredType type, final Object framework) {
        final String statement = "new $T($N)";

        final ClassName className = ClassName.get((TypeElement) type.asElement());
        final List<Object> arguments = ImmutableList.of(serializerClassFor(className), framework);

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                builder.assign(statement, arguments);
            }
        };
    }

    FrameworkStatement resolveParameterizedType(DeclaredType type, Object framework) {
        for (final ParameterizedTypeStatement p : parameterized) {

            if (p.rawType.equals(ClassName.get((TypeElement)type.asElement()))) {
                final Iterator<? extends TypeMirror> types = type.getTypeArguments().iterator();
                final ImmutableList.Builder<FrameworkStatement> statements = ImmutableList.builder();

                for (int i = 0; i < p.parameterCount; i++) {
                    statements.add(resolveStatement(types.next(), framework));
                }

                return resolveGeneric(p.statement, ImmutableList.of(framework), statements.build());
            }
        }

        throw new IllegalArgumentException("Unsupported type: " + type.toString());
    }

    static class ParameterizedTypeStatement {
        final ClassName rawType;
        final String statement;
        final int parameterCount;

        public ParameterizedTypeStatement(ClassName rawType, String statement, int parameterCount) {
            this.rawType = rawType;
            this.statement = statement;
            this.parameterCount = parameterCount;
        }
    }

    static class PrimitiveArrayStatement {
        final TypeName type;
        final String statement;

        public PrimitiveArrayStatement(TypeName type, String statement) {
            this.type = type;
            this.statement = statement;
        }
    }
}
