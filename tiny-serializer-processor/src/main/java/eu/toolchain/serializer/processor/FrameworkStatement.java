package eu.toolchain.serializer.processor;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ParameterSpec;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FrameworkStatement {
  private final String format;
  private final List<Object> arguments;
  private final List<ParameterSpec> providedFields;

  public static FrameworkStatement create(final String format) {
    return create(format, ImmutableList.of(), ImmutableList.of());
  }

  public static FrameworkStatement create(final String format, final List<Object> arguments) {
    return create(format, arguments, ImmutableList.of());
  }

  public static FrameworkStatement create(
    final String format, final List<Object> arguments, final List<ParameterSpec> providedFields
  ) {
    final List<Object> resultArguments = new ArrayList<>();
    final List<String> formatArguments = new ArrayList<>();
    final List<ParameterSpec> allProvidedFields = new ArrayList<>();

    for (final Object argument : arguments) {
      if (argument instanceof FrameworkStatement) {
        final FrameworkStatement statement = ((FrameworkStatement) argument);
        formatArguments.add(statement.getFormat());
        resultArguments.addAll(statement.getArguments());
        allProvidedFields.addAll(statement.getProvidedFields());
        continue;
      }

      resultArguments.add(argument);
    }

    allProvidedFields.addAll(providedFields);

    final String resultFormat = MessageFormat.format(format, formatArguments.toArray());
    return new FrameworkStatement(resultFormat, resultArguments, allProvidedFields);
  }
}
