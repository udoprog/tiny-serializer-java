package eu.toolchain.serializer.processor.value;

import lombok.Data;

import javax.lang.model.type.DeclaredType;

@Data
public class ValueSubType {
    final DeclaredType type;
    final short id;
}
