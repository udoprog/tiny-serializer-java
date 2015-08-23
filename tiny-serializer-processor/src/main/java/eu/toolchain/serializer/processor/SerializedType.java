package eu.toolchain.serializer.processor;

import com.squareup.javapoet.JavaFile;

interface SerializedType {
    public JavaFile asJavaFile();
}