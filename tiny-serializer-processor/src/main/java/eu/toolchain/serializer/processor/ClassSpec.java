package eu.toolchain.serializer.processor;

import com.squareup.javapoet.JavaFile;

public interface ClassSpec {
  JavaFile toSerializer();
}
