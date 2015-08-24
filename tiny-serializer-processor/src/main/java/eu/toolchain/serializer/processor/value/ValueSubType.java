package eu.toolchain.serializer.processor.value;

import lombok.Data;

import com.squareup.javapoet.ClassName;

@Data
public class ValueSubType {
    final ClassName type;
    final short id;
}