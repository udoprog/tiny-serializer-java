package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FrameworkStatements {
    static final Map<TypeName, String> direct = new HashMap<>();

    static {
        direct.put(ClassName.get(String.class), "$N.string()");
        direct.put(ClassName.get(Byte.class), "$N.fixedByte()");
        direct.put(ClassName.get(Short.class), "$N.fixedShort()");
        direct.put(ClassName.get(Integer.class), "$N.fixedInteger()");
        direct.put(ClassName.get(Long.class), "$N.fixedLong()");
        direct.put(ClassName.get(Float.class), "$N.fixedFloat()");
        direct.put(ClassName.get(Double.class), "$N.fixedDouble()");
        direct.put(ClassName.get(Character.class), "$N.fixedCharacter()");
        direct.put(ClassName.get(Boolean.class), "$N.fixedBoolean()");
        direct.put(ClassName.get(UUID.class), "$N.uuid()");
        direct.put(TypeName.get(boolean[].class), "$N.booleanArray()");
        direct.put(TypeName.get(byte[].class), "$N.byteArray()");
        direct.put(TypeName.get(short[].class), "$N.shortArray()");
        direct.put(TypeName.get(int[].class), "$N.intArray()");
        direct.put(TypeName.get(long[].class), "$N.longArray()");
        direct.put(TypeName.get(float[].class), "$N.floatArray()");
        direct.put(TypeName.get(double[].class), "$N.doubleArray()");
        direct.put(TypeName.get(char[].class), "$N.charArray()");
    }

    static final Map<TypeName, Parameterized> parameterized = new HashMap<>();

    static {
        parameterized.put(ClassName.get(List.class), new Parameterized("$N.list", 1));
        parameterized.put(ClassName.get(Map.class), new Parameterized("$N.map", 2));
        parameterized.put(ClassName.get(SortedMap.class), new Parameterized("$N.sortedMap", 2, 2));
        parameterized.put(ClassName.get(NavigableMap.class), new Parameterized("$N.navigableMap", 2, 2));
        parameterized.put(ClassName.get(Set.class), new Parameterized("$N.set", 1));
        parameterized.put(ClassName.get(SortedSet.class), new Parameterized("$N.sortedSet", 1, 2));
        parameterized.put(ClassName.get(NavigableSet.class), new Parameterized("$N.navigableSet", 1, 2));
        parameterized.put(ClassName.get(Optional.class), new Parameterized("$N.optional", 1));
    }

    private final AutoSerializeUtils utils;

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

        if (type.getKind() != TypeKind.DECLARED) {
            throw new IllegalArgumentException("Cannot handle type: " + type);
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

    private FrameworkStatement resolveArrayType(final ArrayType a, final Object framework) {
        final TypeMirror componentType = a.getComponentType();

        if (utils.isPrimitive(componentType)) {
            throw new IllegalArgumentException("Cannot serialize array with a primitive component type: " + a);
        }

        final FrameworkStatement component = resolveStatement(utils.boxedIfNeeded(componentType), framework);

        final TypeName innerMost = TypeName.get(arrayInnerMost(componentType));
        // Get parenthesis combination after the size parameter.
        final String parens = arrayParensAfterSize(componentType);

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                component.writeTo((cs, ca) -> {
                    final List<Object> arguments = new ArrayList<>();

                    arguments.add(framework);
                    arguments.add(componentType);
                    arguments.addAll(ca);
                    arguments.add(innerMost);

                    builder.assign(String.format("$N.<$T>array(%s, (s) -> new $T[s]%s)", cs, parens), arguments);
                });
            }
        };
    }

    private String arrayParensAfterSize(TypeMirror t) {
        return t.accept(new SimpleTypeVisitor6<String, Void>() {
            @Override
            public String visitArray(ArrayType t, Void p) {
                return "[]" + arrayParensAfterSize(t.getComponentType());
            }

            @Override
            protected String defaultAction(TypeMirror e, Void p) {
                return "";
            }
        }, null);
    }

    private TypeMirror arrayInnerMost(TypeMirror t) {
        return t.accept(new SimpleTypeVisitor6<TypeMirror, Void>() {
            @Override
            public TypeMirror visitArray(ArrayType t, Void p) {
                return arrayInnerMost(t.getComponentType());
            }

            @Override
            protected TypeMirror defaultAction(TypeMirror e, Void p) {
                return e;
            }
        }, null);
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

        final List<Object> arguments = ImmutableList.of(utils.serializerClassFor(type), framework);

        return new FrameworkStatement() {
            @Override
            public void writeTo(FrameworkMethodBuilder builder) {
                builder.assign(statement, arguments);
            }
        };
    }

    private ParameterizedMatch findBestMatch(DeclaredType type) {
        final Queue<TypeMirror> types = new LinkedList<>();

        types.add(type);

        final SortedSet<ParameterizedMatch> matches = new TreeSet<>();

        while (!types.isEmpty()) {
            final DeclaredType t = (DeclaredType) types.poll();

            final TypeElement e = (TypeElement) t.asElement();
            final ClassName c = ClassName.get(e);

            final Parameterized p = parameterized.get(c);

            if (p == null) {
                break;
            }

            matches.add(new ParameterizedMatch(p, t));
        }

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Type not supported: " + type);
        }

        return matches.last();
    }

    private FrameworkStatement resolveParameterizedType(DeclaredType type, Object framework) {
        final ParameterizedMatch p = findBestMatch(type);

        final Iterator<? extends TypeMirror> typeArguments = p.type.getTypeArguments().iterator();
        final ImmutableList.Builder<FrameworkStatement> statements = ImmutableList.builder();

        for (int i = 0; i < p.parameterized.parameterCount; i++) {
            statements.add(resolveStatement(typeArguments.next(), framework));
        }

        return resolveGeneric(p.parameterized.statement, ImmutableList.of(framework), statements.build());
    }

    static class Parameterized implements Comparable<Parameterized> {
        final String statement;
        final int parameterCount;
        final int priority;

        public Parameterized(String statement, int parameterCount) {
            this(statement, parameterCount, 0);
        }

        public Parameterized(String statement, int parameterCount, int priority) {
            this.statement = statement;
            this.parameterCount = parameterCount;
            this.priority = priority;
        }

        @Override
        public int compareTo(Parameterized o) {
            return Integer.compare(priority, o.priority);
        }
    }

    @RequiredArgsConstructor
    static class ParameterizedMatch implements Comparable<ParameterizedMatch> {
        final Parameterized parameterized;
        final DeclaredType type;

        @Override
        public int compareTo(ParameterizedMatch o) {
            return parameterized.compareTo(o.parameterized);
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
