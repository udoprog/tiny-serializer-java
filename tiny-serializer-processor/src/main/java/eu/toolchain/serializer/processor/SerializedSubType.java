package eu.toolchain.serializer.processor;

import lombok.Data;

import com.squareup.javapoet.ClassName;

@Data
class SerializedSubType {
    final ClassName type;
    final short id;
}