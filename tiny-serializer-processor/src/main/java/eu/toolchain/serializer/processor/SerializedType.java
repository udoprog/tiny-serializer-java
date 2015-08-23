package eu.toolchain.serializer.processor;

import java.util.List;

import javax.lang.model.element.Element;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import lombok.Data;

@Data
class SerializedType {
    final Element root;
    final String packageName;
    final String name;
    final TypeSpec type;
    final TypeName elementType;
    final TypeName supertype;
    final SerializedFields fields;

    public JavaFile asJavaFile() {
        return JavaFile.builder(packageName, type).skipJavaLangImports(true).indent("    ").build();
    }

    public List<SerializedTypeError> validate() {
        return fields.validate(root);
    }
}