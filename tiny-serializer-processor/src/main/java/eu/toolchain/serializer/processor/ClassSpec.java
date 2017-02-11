package eu.toolchain.serializer.processor;

import com.squareup.javapoet.JavaFile;
import eu.toolchain.serializer.processor.field.FieldSet;

public interface ClassSpec {
  JavaFile toSerializer();

  FieldSet getFieldSet();
}
