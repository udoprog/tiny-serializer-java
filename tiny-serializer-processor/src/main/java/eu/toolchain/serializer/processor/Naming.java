package eu.toolchain.serializer.processor;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * Provides utilities for generating unique, readable names from given types.
 *
 * @author udoprog
 */
@RequiredArgsConstructor
public class Naming {
  static final ClassName optionalType = ClassName.get(Optional.class);

  private final String prefix;

  private final Set<String> seen = new HashSet<>();

  public String forName(String name) {
    return makeUnique(String.format("%s%s", prefix, name));
  }

  public String forType(TypeName fieldType, boolean provided) {
    return makeUnique(
      String.format(provided ? "%sProvided%s" : "%s%s", prefix, composeName(fieldType)));
  }

  private String makeUnique(final String base) {
    int index = 1;

    String candidate = base;

    while (seen.contains(candidate)) {
      candidate = String.format("%s%d", base, index++);
    }

    seen.add(candidate);
    return candidate;
  }

  private String composeName(TypeName fieldType) {
    if (fieldType instanceof ClassName) {
      final ClassName c = (ClassName) fieldType;
      return c.simpleName();
    }

    if (fieldType instanceof ParameterizedTypeName) {
      final ParameterizedTypeName p = (ParameterizedTypeName) fieldType;

      if (p.rawType.equals(optionalType)) {
        return "Optional" + composeName(p.typeArguments.iterator().next());
      }

      return p.rawType.simpleName();
    }

    if (fieldType instanceof ArrayTypeName) {
      final ArrayTypeName a = (ArrayTypeName) fieldType;
      return composeName(a.componentType) + "Array";
    }

    if (fieldType.isPrimitive()) {
      return composePrimitive(fieldType);
    }

    throw new IllegalArgumentException("Cannot compose name from " + fieldType.toString());
  }

  private String composePrimitive(TypeName fieldType) {
    if (fieldType.equals(TypeName.BOOLEAN)) {
      return "Boolean";
    }

    if (fieldType.equals(TypeName.BYTE)) {
      return "Byte";
    }

    if (fieldType.equals(TypeName.SHORT)) {
      return "Short";
    }

    if (fieldType.equals(TypeName.INT)) {
      return "Integer";
    }

    if (fieldType.equals(TypeName.LONG)) {
      return "Long";
    }

    if (fieldType.equals(TypeName.CHAR)) {
      return "Character";
    }

    if (fieldType.equals(TypeName.FLOAT)) {
      return "Float";
    }

    if (fieldType.equals(TypeName.DOUBLE)) {
      return "Double";
    }

    throw new IllegalArgumentException("Unsupported primitive type: " + fieldType);
  }
}
