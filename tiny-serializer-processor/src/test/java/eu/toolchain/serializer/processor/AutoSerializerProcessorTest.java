package eu.toolchain.serializer.processor;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

public class AutoSerializerProcessorTest {
    @Test
    public void testEmpty() {
        verifySerializer("Empty");
    }

    @Test
    public void testInterface() {
        verifySerializer("Interface");
    }

    @Test
    public void testAbstract() {
        verifySerializer("Abstract");
    }

    @Test
    public void testFields() {
        verifySerializer("Fields");
    }

    @Test
    public void testGetter() {
        verifySerializer("Getter");
    }

    @Test
    public void testCustomAccessor() {
        verifySerializer("CustomAccessor");
    }

    @Test
    public void testImplicitConstructor() {
        verifySerializer("ImplicitConstructor");
    }

    @Test
    public void testCollections() {
        verifySerializer("Collections");
    }

    @Test
    public void testIgnore() {
        verifySerializer("Ignore");
    }

    @Test
    public void testProvided() {
        verifySerializer("Provided");
    }

    @Test
    public void testOptional() {
        verifySerializer("OptionalTest");
    }

    @Test
    public void testDuplicateTypes() {
        verifySerializer("DuplicateTypes");
    }

    @Test
    public void testUseBuilder() {
        verifySerializer("UseBuilder");
    }

    @Test
    public void testUseBuilderConstructor() {
        verifySerializer("UseBuilderConstructor");
    }

    @Test
    public void testOrdering() {
        verifySerializer("Ordering");
    }

    @Test
    public void testNested() {
        verifySerializer("Nested", "Nested_Foo_Serializer");
    }

    static void verifySerializer(String name) {
        verifySerializer(name, String.format(FrameworkStatements.SERIALIZER_NAME_FORMAT, name));
    }

    static void verifySerializer(String sourceName, String first, String... rest) {
        final JavaFileObject source = resourcePathFor(sourceName);
        final JavaFileObject firstSerializer = resourcePathFor(first);

        final JavaFileObject restSerializers[] = new JavaFileObject[rest.length];

        for (int i = 0; i < rest.length; i++) {
            restSerializers[i] = resourcePathFor(rest[i]);
        }

        assert_().about(javaSource()).that(source).processedWith(new AutoSerializeProcessor()).compilesWithoutError()
                .and().generatesSources(firstSerializer, restSerializers);
    }

    static void verifyFailingSerializer(String name) {
        final JavaFileObject source = resourcePathFor(name);
        assert_().about(javaSource()).that(source).processedWith(new AutoSerializeProcessor()).failsToCompile();
    }

    static JavaFileObject resourcePathFor(String name) {
        final String dirName = AutoSerializerProcessorTest.class.getPackage().getName().replace('.', '/');
        return JavaFileObjects.forResource(String.format("%s/%s.java", dirName, name));
    }
}