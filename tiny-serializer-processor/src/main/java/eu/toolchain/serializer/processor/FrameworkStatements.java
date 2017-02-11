package eu.toolchain.serializer.processor;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.field.FieldSet;
import eu.toolchain.serializer.processor.field.FieldType;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FrameworkStatements {
  private static final Joiner ARGUMENT_JOINER = Joiner.on(", ");
  private static final Map<TypeName, String> DIRECT = new HashMap<>();

  static {
    DIRECT.put(ClassName.get(String.class), "$N.string()");
    DIRECT.put(ClassName.get(Byte.class), "$N.fixedByte()");
    DIRECT.put(ClassName.get(Short.class), "$N.fixedShort()");
    DIRECT.put(ClassName.get(Integer.class), "$N.fixedInteger()");
    DIRECT.put(ClassName.get(Long.class), "$N.fixedLong()");
    DIRECT.put(ClassName.get(Float.class), "$N.fixedFloat()");
    DIRECT.put(ClassName.get(Double.class), "$N.fixedDouble()");
    DIRECT.put(ClassName.get(Character.class), "$N.fixedCharacter()");
    DIRECT.put(ClassName.get(Boolean.class), "$N.fixedBoolean()");
    DIRECT.put(ClassName.get(UUID.class), "$N.uuid()");
    DIRECT.put(TypeName.get(boolean[].class), "$N.booleanArray()");
    DIRECT.put(TypeName.get(byte[].class), "$N.byteArray()");
    DIRECT.put(TypeName.get(short[].class), "$N.shortArray()");
    DIRECT.put(TypeName.get(int[].class), "$N.intArray()");
    DIRECT.put(TypeName.get(long[].class), "$N.longArray()");
    DIRECT.put(TypeName.get(float[].class), "$N.floatArray()");
    DIRECT.put(TypeName.get(double[].class), "$N.doubleArray()");
    DIRECT.put(TypeName.get(char[].class), "$N.charArray()");
  }

  static final Map<TypeName, Parameterized> PARAMETERIZED = new HashMap<>();

  static {
    PARAMETERIZED.put(ClassName.get(List.class), new Parameterized("$N.list", 1));
    PARAMETERIZED.put(ClassName.get(Map.class), new Parameterized("$N.map", 2));
    PARAMETERIZED.put(ClassName.get(SortedMap.class), new Parameterized("$N.sortedMap", 2, 2));
    PARAMETERIZED.put(ClassName.get(NavigableMap.class),
      new Parameterized("$N.navigableMap", 2, 2));
    PARAMETERIZED.put(ClassName.get(Set.class), new Parameterized("$N.set", 1));
    PARAMETERIZED.put(ClassName.get(SortedSet.class), new Parameterized("$N.sortedSet", 1, 2));
    PARAMETERIZED.put(ClassName.get(NavigableSet.class),
      new Parameterized("$N.navigableSet", 1, 2));
    PARAMETERIZED.put(ClassName.get(Optional.class), new Parameterized("$N.optional", 1));
  }

  private final AutoSerializeUtils utils;

  public FrameworkStatement resolveStatement(TypeMirror type) {
    type = utils.boxedIfNeeded(type);

    final String statement = DIRECT.get(TypeName.get(type));

    if (statement != null) {
      return (fields, framework) -> {
        return builder -> builder.assign(statement, ImmutableList.of(framework));
      };
    }

    if (type instanceof ArrayType) {
      final ArrayType a = (ArrayType) type;
      return resolveArrayType(a);
    }

    if (type.getKind() != TypeKind.DECLARED) {
      throw new IllegalArgumentException("Cannot handle type: " + type);
    }

    final DeclaredType d = (DeclaredType) type;

    if (d.asElement().getKind() == ElementKind.ENUM) {
      return resolveEnum((TypeElement) d.asElement());
    }

    if (d.getTypeArguments().isEmpty()) {
      return resolveCustomSerializer(d);
    }

    return resolveParameterizedType(d);
  }

  private FrameworkStatement resolveArrayType(final ArrayType a) {
    final TypeMirror componentType = a.getComponentType();

    if (utils.isPrimitive(componentType)) {
      throw new IllegalArgumentException(
        "Cannot serialize array with a primitive component type: " + a);
    }

    final FrameworkStatement component = resolveStatement(utils.boxedIfNeeded(componentType));

    final TypeName innerMost = TypeName.get(arrayInnerMost(componentType));
    // Get parenthesis combination after the size parameter.
    final String parens = arrayParensAfterSize(componentType);

    return (fields, framework) -> {
      final FrameworkStatement.Instance c = component.build(fields, framework);

      return builder -> {
        c.writeTo((cs, ca) -> {
          final List<Object> arguments = new ArrayList<>();

          arguments.add(framework);
          arguments.add(componentType);
          arguments.addAll(ca);
          arguments.add(innerMost);

          builder.assign(String.format("$N.<$T>array(%s, (s) -> new $T[s]%s)", cs, parens),
            arguments);
        });
      };
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

  private FrameworkStatement resolveEnum(final TypeElement element) {
    final ClassName enumType = ClassName.get(element);

    return (fields, framework) -> {
      return builder -> {
        builder.assign("$N.forEnum($T.values())", ImmutableList.of(framework, enumType));
      };
    };
  }

  FrameworkStatement resolveGeneric(
    final String statementBase, final List<Object> argumentsBase,
    final List<FrameworkStatement.Instance> statements
  ) {
    return (fields, framework) -> {
      final List<String> typeStatements = new ArrayList<>();

      final ImmutableList.Builder<Object> outerArguments = ImmutableList.builder();

      outerArguments.addAll(argumentsBase);

      statements.forEach(instance -> {
        instance.writeTo((statement, arguments) -> {
          typeStatements.add(statement);
          outerArguments.addAll(arguments);
        });
      });

      final String statement =
        String.format("%s(%s)", statementBase, ARGUMENT_JOINER.join(typeStatements));
      final List<Object> arguments = outerArguments.build();

      return builder -> builder.assign(statement, arguments);
    };
  }

  FrameworkStatement resolveCustomSerializer(final DeclaredType type) {
    return new FrameworkStatement() {
      @Override
      public Instance build(final Optional<FieldSet> fields, final Object framework) {
        return builder -> {
          final List<FieldType> fieldTypes =
            fields.map(FieldSet::getAllOrderedTypes).orElseGet(ImmutableList::of);

          final List<String> parameterFormat = new ArrayList<>();
          parameterFormat.add("$N");

          final ImmutableList.Builder<Object> arguments = ImmutableList.builder();
          arguments.add(utils.serializerClassFor(type));
          arguments.add(framework);

          fieldTypes.forEach(p -> {
            p.getProvidedParameterSpec().ifPresent(s -> {
              parameterFormat.add("$N");
              arguments.add(s);
            });
          });

          final String statement =
            String.format("new $T(%s)", ARGUMENT_JOINER.join(parameterFormat));

          builder.assign(statement, arguments.build());
        };
      }

      @Override
      public boolean isCustom() {
        return true;
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

      final Parameterized p = PARAMETERIZED.get(c);

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

  private FrameworkStatement resolveParameterizedType(DeclaredType type) {
    return (fields, framework) -> {
      final ParameterizedMatch p = findBestMatch(type);

      final Iterator<? extends TypeMirror> typeArguments = p.type.getTypeArguments().iterator();
      final ImmutableList.Builder<FrameworkStatement.Instance> statements = ImmutableList.builder();

      for (int i = 0; i < p.parameterized.parameterCount; i++) {
        statements.add(resolveStatement(typeArguments.next()).build(fields, framework));
      }

      return resolveGeneric(p.parameterized.statement, ImmutableList.of(framework),
        statements.build()).build(fields, framework);
    };
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
}
