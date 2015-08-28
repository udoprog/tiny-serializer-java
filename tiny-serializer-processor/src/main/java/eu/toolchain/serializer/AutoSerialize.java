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
     * Use field-based serialization which encodes the name of the field in the message stream.
     *
     * @return {@code true} if field-based serialization should be used.
     */
    boolean fieldBased() default false;

    /**
     * Fail during de-serialization if a field is seen that is not part of this object.
     *
     * The default behaviour is to fail.
     *
     * @return {@code true} if the de-serialize method should throws exception on missing fields.
     */
    boolean failOnMissing() default true;

    /**
     * Use getter to access fields.
     *
     * @return {@code true} if getters should be used, {@code false} otherwise.
     */
    boolean useGetter() default true;

    /**
     * Use builder when creating instance (instead of implicit constructor).
     *
     * @return If non-empty, will use the first configured builder, otherwise will use constructor.
     */
    Builder[] builder() default {};

    /**
     * Order field serialization by id.
     *
     * @return {@code true} if serialization should be ordered by id, {@code false} othwerwise.
     */
    boolean orderById() default true;

    boolean orderConstructorById() default false;

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Builder {
        /**
         * Use the specified builder class instead of assuming the serialized type has a builder.
         *
         * @return Builder type to use, or {@link DefaultBuilderType} if none is specified.
         */
        Class<?> type() default DefaultBuilderType.class;

        /**
         * Use setters when assigning values to a builder.
         *
         * Default behavior is to assume the builder has methods named the same as the field.
         *
         * @return {@code true} if builders use setters, {@code false} otherwise.
         */
        boolean useSetter() default false;

        /**
         * Use builder constructor when constructing builder.
         *
         * The default method would otherwise be to create a builder instance using {@link #useBuilderMethod()}.
         *
         * @return {@code true} if constructor should be used for builder type, {@code false} otherwise.
         */
        boolean useConstructor() default false;

        /**
         * Use method on builder type when constructing builder.
         * This takes precedence over {@link #useConstructor()}
         *
         * This only makes sense if the builder type itself has a static builder method (like MyType.builder()).
         *
         * @return {@code true} if builder method should be used for builder type, {@code false} otherwise.
         */
        boolean useMethod() default false;

        /**
         * Builder method to use unless {@link #useBuilderConstructor()} is true.
         *
         * @return Builder method to use, or empty string if not specified;
         */
        String methodName() default "builder";
    }

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

    @Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Field {
        /**
         * Override serialized name.
         *
         * @return The configured name, or empty string if none is configured.
         */
        String name() default "";

        /**
         * Override field name.
         *
         * By default, the field name is derived from the name of the field or method.
         *
         * @return The configured field name, or empty string if none is configured.
         */
        String fieldName() default "";

        /**
         * Override accessor name.
         *
         * By default, the accessor will be the same as the field name. If {@link #useGetter()} or
         * {@link AutoSerialize#useGetter()} is {@code true}, will use a getter derived from the name.
         *
         * @return The configured accessor, or empty string if none is configured.
         */
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

    @Target({ ElementType.FIELD, ElementType.METHOD })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Ignore {
    }
}