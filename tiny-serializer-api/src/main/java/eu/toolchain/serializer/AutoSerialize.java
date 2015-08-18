package eu.toolchain.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoSerialize {
    String name() default "";

    /**
     * Use getter to access fields.
     * 
     * @return {@code true} if getters should be used, {@code false} otherwise.
     */
    boolean useGetter() default true;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubTypes {
        SubType[] value() default {};
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubType {
        Class<?> value();

        short id() default -1;
    }

    @Target(ElementType.CONSTRUCTOR)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Creator {
        String value() default "";

        boolean optional() default false;
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Field {
        String accessor() default "";

        boolean useGetter() default false;
    }
}