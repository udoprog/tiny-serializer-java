package eu.toolchain.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoSerialize {
    String name() default "";

    /**
     * Use getter to access fields.
     *
     * @return {@code true} if getters should be used, {@code false} otherwise.
     */
    boolean useGetter() default true;

    /**
     * Use builder when creating instance (instead of implicit constructor).
     *
     * @return {@code true} if builders should be used, {@code false} otherwise.
     */
    boolean useBuilder() default false;

    /**
     * Use setters when assigning values to a builder.
     *
     * @return {@code true} if builders use setters, {@code false} otherwise.
     */
    boolean useBuilderSetter() default false;

    /**
     * Order field serialization by id.
     *
     * @return {@code true} if serialization should be ordered by id, {@code false} othwerwise.
     */
    boolean orderById() default true;

    boolean orderConstructorById() default false;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface SubTypes {
        SubType[] value() default {};
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface SubType {
        Class<?> value();

        short id() default -1;
    }

    @Target({ ElementType.PARAMETER, ElementType.FIELD })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Field {
        String accessor() default "";

        /**
         * Control ordering of the fields.
         *
         * @return A number indicating how the field should be ordered relative to other fields.
         */
        int id() default -1;

        /**
         * Control ordering of the constructor parameters.
         *
         * @return A number indicating how the field should be ordered relative to other fields.
         */
        int constructorOrder() default -1;

        /**
         * Use a getter-like method name to to fetch the value of the field.
         *
         * @return {@code true} if a getter-like name should be used, {@code false} otherwise.
         */
        boolean useGetter() default true;

        /**
         * Indicate that the annotated field should be provided in the construction of the serializer.
         */
        boolean provided() default false;

        /**
         * Specify a specific provider name.
         */
        String providerName() default "";
    }

    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Ignore {
    }
}