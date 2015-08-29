package eu.toolchain.serializer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import lombok.Data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import eu.toolchain.serializer.SerializerFramework.TypeMapping;
import eu.toolchain.serializer.io.CoreBytesSerialWriter;
import eu.toolchain.serializer.io.CoreByteArraySerialReader;

@RunWith(MockitoJUnitRunner.class)
public class SubtypesSerializerTest {
    final SerializerFramework s = TinySerializer.builder().build();

    final Serializer<A> a = new Serializer<A>() {
        final Serializer<Integer> number = s.integer();

        @Override
        public void serialize(SerialWriter buffer, A value) throws IOException {
            this.number.serialize(buffer, value.getNumber());
        }

        @Override
        public A deserialize(SerialReader buffer) throws IOException {
            final int number = this.number.deserialize(buffer);
            return new A(number);
        }
    };

    final Serializer<B> b = s.singleton(new B());

    @Test
    public void testBasic() throws IOException {
        final ImmutableList.Builder<TypeMapping<? extends Parent, Parent>> children = ImmutableList.builder();

        children.add(s.<A, Parent> type(1, A.class, a));
        children.add(s.<B, Parent> type(2, B.class, b));

        Serializer<Parent> parent = s.subtypes(children.build());

        final CoreBytesSerialWriter writer = new CoreBytesSerialWriter();
        final A ref1 = new A(42);
        final B ref2 = new B();

        parent.serialize(writer, ref1);
        parent.serialize(writer, ref2);

        final SerialReader reader = new CoreByteArraySerialReader(writer.toByteArray());
        assertEquals(ref1, parent.deserialize(reader));
        assertEquals(ref2, parent.deserialize(reader));
    }

    static interface Parent {
    }

    @Data
    static class A implements Parent {
        final int number;
    }

    @Data
    static class B implements Parent {
    }
}