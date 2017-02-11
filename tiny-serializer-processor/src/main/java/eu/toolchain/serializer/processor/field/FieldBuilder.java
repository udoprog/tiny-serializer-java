package eu.toolchain.serializer.processor.field;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.annotation.BuilderMirror;
import java.util.List;
import lombok.Data;

/**
 * @author udoprog
 */
@Data
public class FieldBuilder {
  static final Joiner emptyJoiner = Joiner.on("");

  final BuilderMirror builder;

  /**
   * If {@code true}, indicates that the constructor of the builder should be used when
   * instantiating it.
   */
  final boolean useConstructor;

  /**
   * If {@code true}, indicates that builder methods are setters instead of using a one-to-one
   * mapping of field names.
   */
  final boolean useSetter;

  /**
   * Contains the name of the method to use, unless {@link #useConstructor} is {@code true}.
   */
  final String method;

  public void writeTo(ClassName returnType, MethodSpec.Builder b, List<Value> variables) {
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

    for (final Value f : variables) {
      builders.add(String.format(".%s($L)", builderSetter(f)));
      parameters.add(f.getVariableName());
    }

    b.addStatement(
      String.format("return %s%s.build()", builderStatement, emptyJoiner.join(builders.build())),
      parameters.build().toArray());
  }

  TypeName getBuilderTypeForConstructor(ClassName returnType) {
    return builder
      .getType()
      .map((t) -> TypeName.get(t.get()))
      .orElse(returnType.nestedClass("Builder"));
  }

  TypeName getBuilderTypeForMethod(ClassName returnType) {
    return builder.getType().map((t) -> TypeName.get(t.get())).orElse(returnType);
  }

  String builderSetter(final Value f) {
    if (useSetter) {
      return "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, f.getName());
    }

    return f.getName();
  }
}
