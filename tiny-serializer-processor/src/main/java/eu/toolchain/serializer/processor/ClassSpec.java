package eu.toolchain.serializer.processor;

import com.squareup.javapoet.JavaFile;
import eu.toolchain.serializer.processor.field.Field;
import eu.toolchain.serializer.processor.field.Value;
import java.util.List;

public interface ClassSpec {
  JavaFile toSerializer();

  List<Value> getValues();

  List<Field> getFields();
}
