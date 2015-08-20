package eu.toolchain.serializer.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

import lombok.Data;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

@Data
class SerializedType {
    final Element root;
    final String packageName;
    final String name;
    final TypeSpec type;
    final TypeName elementType;
    final TypeName supertype;
    final SerializedTypeFields fields;

    public JavaFile asJavaFile() {
        return JavaFile.builder(packageName, type).skipJavaLangImports(true).indent("    ").build();
    }

    public boolean isValid(Messager messager) {
        return fields.isValid(root, messager);
    }
}