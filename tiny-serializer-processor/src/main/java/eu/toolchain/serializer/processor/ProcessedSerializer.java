package eu.toolchain.serializer.processor;

import java.util.List;

import lombok.Data;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

@Data
class ProcessedSerializer {
    final String packageName;
    final String name;
    final TypeSpec type;
    final TypeName elementType;
    final TypeName supertype;

    final List<SerializedFieldType> fieldTypes;

    public JavaFile asJavaFile() {
        return JavaFile.builder(packageName, type).skipJavaLangImports(true).indent("    ").build();
    }
}