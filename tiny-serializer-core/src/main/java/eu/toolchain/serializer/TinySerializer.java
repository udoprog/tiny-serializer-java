package eu.toolchain.serializer;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Supplier;

import eu.toolchain.serializer.io.ByteArraySerializer;
import eu.toolchain.serializer.io.ByteBufferSerialReader;
import eu.toolchain.serializer.io.ByteBufferSerialWriter;
import eu.toolchain.serializer.io.CharArraySerializer;
import eu.toolchain.serializer.types.BooleanSerializer;
import eu.toolchain.serializer.types.CompactVarIntSerializer;
import eu.toolchain.serializer.types.CompactVarLongSerializer;
import eu.toolchain.serializer.types.DoubleSerializer;
import eu.toolchain.serializer.types.FloatSerializer;
import eu.toolchain.serializer.types.IntegerSerializer;
import eu.toolchain.serializer.types.LengthPrefixedSerializer;
import eu.toolchain.serializer.types.ListSerializer;
import eu.toolchain.serializer.types.LongSerializer;
import eu.toolchain.serializer.types.MapSerializer;
import eu.toolchain.serializer.types.NullSerializer;
import eu.toolchain.serializer.types.OptionalSerializer;
import eu.toolchain.serializer.types.OrdinalEnumSerializer;
import eu.toolchain.serializer.types.PrefixSerializer;
import eu.toolchain.serializer.types.SetSerializer;
import eu.toolchain.serializer.types.ShortSerializer;
import eu.toolchain.serializer.types.SingletonSerializer;
import eu.toolchain.serializer.types.SortedMapSerializer;
import eu.toolchain.serializer.types.SortedSetSerializer;
import eu.toolchain.serializer.types.StringEnumSerializer;
import eu.toolchain.serializer.types.StringSerializer;
import eu.toolchain.serializer.types.SubTypesSerializer;
import eu.toolchain.serializer.types.UUIDSerializer;
import eu.toolchain.serializer.types.VarIntSerializer;
import eu.toolchain.serializer.types.VarLongSerializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TinySerializer implements SerializerFramework {
    public static final Serializer<Integer> DEFAULT_INTEGER = new CompactVarIntSerializer();
    public static final LengthPolicy DEFAULT_LENGTH_POLICY = new MaxLengthPolicy(Integer.MAX_VALUE);

    private final Serializer<Integer> size;
    private final Serializer<Integer> subTypeId;
    private final Serializer<Integer> enumOrdinal;

    private final LengthPolicy defaultLengthPolicy;

    private final Serializer<byte[]> byteArray;
    private final Serializer<char[]> charArray;
    private final Serializer<String> string;

    private final Serializer<Boolean> bool;
    private final Serializer<Short> shortNumber;
    private final Serializer<Integer> integer;
    private final Serializer<Long> longNumber;
    private final Serializer<Float> floatNumber;
    private final Serializer<Double> doubleNumber;

    private final Serializer<Integer> varint;
    private final Serializer<Long> varlong;
    private final Serializer<UUID> uuid;

    private final boolean useStringsForEnums;

    private final Serializer<? extends Object> notImplemented = new Serializer<Object>() {
        @Override
        public void serialize(SerialWriter buffer, Object value) throws IOException {
            throw new RuntimeException("not implemented");
        }

        @Override
        public Object deserialize(SerialReader buffer) throws IOException {
            throw new RuntimeException("not implemented");
        }
    };

    /* primitive serializers */

    @Override
    public Serializer<String> string() {
        return string;
    }

    @Override
    public Serializer<Boolean> bool() {
        return bool;
    }

    @Override
    public Serializer<Integer> varint() {
        return varint;
    }

    @Override
    public Serializer<Long> varlong() {
        return varlong;
    }

    @Override
    public Serializer<Integer> integer() {
        return integer;
    }

    @Override
    public Serializer<Short> shortNumber() {
        return shortNumber;
    }

    @Override
    public Serializer<Long> longNumber() {
        return longNumber;
    }

    @Override
    public Serializer<Float> floatNumber() {
        return floatNumber;
    }

    @Override
    public Serializer<Double> doubleNumber() {
        return doubleNumber;
    }

    /* more fancy things */

    /**
     * Build a serializer that is capable of serializing null values.
     *
     * @return
     */
    @Override
    public <T> Serializer<T> nullable(Serializer<T> serializer) {
        return new NullSerializer<T>(serializer);
    }

    @Override
    public <T> Serializer<T> prefix(int prefix, Serializer<T> serializer) {
        return prefix(ByteBuffer.allocate(Integer.BYTES).putInt(prefix).array(), serializer);
    }

    @Override
    public <T> Serializer<T> prefix(byte[] prefix, Serializer<T> serializer) {
        return new PrefixSerializer<T>(prefix, serializer);
    }

    @Override
    public <T> Serializer<T> lengthPrefixed(Serializer<T> serializer) {
        return lengthPrefixed(serializer, defaultLengthPolicy);
    }

    @Override
    public <T> Serializer<T> lengthPrefixed(Serializer<T> serializer, LengthPolicy policy) {
        return new LengthPrefixedSerializer<T>(varint(), serializer, policy);
    }

    @Override
    public <T> Serializer<List<T>> list(Serializer<T> serializer) {
        return new ListSerializer<T>(size, serializer);
    }

    @Override
    public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value) {
        return new MapSerializer<K, V>(size, key, value);
    }

    @Override
    public <K, V> Serializer<SortedMap<K, V>> sortedMap(Serializer<K> key, Serializer<V> value) {
        return new SortedMapSerializer<K, V>(size, key, value);
    }

    @Override
    public <T> Serializer<Set<T>> set(Serializer<T> serializer) {
        return new SetSerializer<T>(size, serializer);
    }

    @Override
    public <T> Serializer<SortedSet<T>> sortedSet(Serializer<T> serializer) {
        return new SortedSetSerializer<T>(size, serializer);
    }

    @Override
    public Serializer<byte[]> byteArray() {
        return byteArray;
    }

    @Override
    public Serializer<char[]> charArray() {
        return charArray;
    }

    @Override
    public Serializer<UUID> uuid() {
        return uuid;
    }

    @Override
    public <T> Serializer<Optional<T>> optional(Serializer<T> element) {
        return new OptionalSerializer<T>(bool, element);
    }

    @Override
    public <T extends Enum<T>> Serializer<T> forEnum(final T[] values) {
        if (useStringsForEnums) {
            return new StringEnumSerializer<T>(string, values, () -> { throw new IOException("string does not match enum"); });
        } else {
            return new OrdinalEnumSerializer<T>(enumOrdinal, values, () -> { throw new IOException("ordinal out of range"); });
        }
    }

    @Override
    public <T extends Enum<T>> Serializer<T> forEnum(final T[] values, T defaultValue) {
        if (useStringsForEnums) {
            return new StringEnumSerializer<T>(string, values, () -> defaultValue);
        } else {
            return new OrdinalEnumSerializer<T>(enumOrdinal, values, () -> defaultValue);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Serializer<T> notImplemented() {
        return (Serializer<T>) notImplemented;
    }

    @Override
    public <T extends S, S> TypeMapping<T, S> type(int id, Class<T> key, Serializer<T> serializer) {
        if (id >= 0xffff || id < 0) {
            throw new IllegalArgumentException("id must be a positive number smaller than 0xffff");
        }

        return new TypeMapping<T, S>(id, key, serializer);
    }

    @Override
    public final <T> Serializer<T> subtypes(Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        return SubTypesSerializer.fromTypeMappings(subTypeId, mappings);
    }

    @Override
    public <T> Serializer<T> singleton(T value) {
        return new SingletonSerializer<T>(value);
    }

    /* utility functions */

    @Override
    public <T> ByteBuffer serialize(Serializer<T> serializer, T value) throws IOException {
        final ByteBufferSerialWriter buffer = new ByteBufferSerialWriter();
        serializer.serialize(buffer, value);
        buffer.flush();
        return buffer.buffer();
    }

    @Override
    public <T> T deserialize(Serializer<T> serializer, ByteBuffer buffer) throws IOException {
        return serializer.deserialize(new ByteBufferSerialReader(buffer));
    }

    /**
     * Create a new TinySerializer instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Serializer<Integer> size;
        private Serializer<Integer> subTypeId;
        private Serializer<Integer> enumOrdinal;
        private LengthPolicy defaultLengthPolicy;

        private Serializer<byte[]> byteArray;
        private Serializer<char[]> charArray;
        private Serializer<String> string;
        private Serializer<Integer> stringSize;

        private Serializer<Boolean> bool;
        private Serializer<Short> shortNumber;
        private Serializer<Integer> integer;
        private Serializer<Long> longNumber;
        private Serializer<Float> floatNumber;
        private Serializer<Double> doubleNumber;

        private Serializer<Integer> varint;
        private Serializer<Long> varlong;
        private Serializer<UUID> uuid;

        private boolean useSimplerVariableLength;
        private boolean useCompactSize;
        private boolean useStringsForEnums;

        /**
         * Prefer the 'simpler' variable length implementation over the more compact one.
         *
         * The simpler is beneficial when you are inspecting traffic by eye, since it performs a less esoteric encoding
         * of the number.
         *
         * @see VarIntSerializer
         * @see CompactVarIntSerializer
         * @param useCompactVariableLength
         * @return
         */
        public Builder useSimplerVariableLength(boolean useSimplerVariableLength) {
            this.useSimplerVariableLength = useSimplerVariableLength;
            return this;
        }

        /**
         * Use a compact, but less efficient serializer for size-like numbers.
         *
         * Size-like numbers are things which designates sizes, which typically have a small(ish) value. Therefore you
         * can usually save a fair bit of space by using a VLQ-like serialization on them.
         *
         * @param useCompactSize {@code true} will enable compact size serialization.
         * @return This builder.
         */
        public Builder useCompactSize(boolean useCompactSize) {
            this.useCompactSize = useCompactSize;
            return this;
        }

        /**
         * Encode enums as strings, allowing them to be more portable.
         */
        public Builder useStringsForEnums(boolean useStringsForEnums) {
            this.useStringsForEnums = useStringsForEnums;
            return this;
        }

        /**
         * Set serializer to use for size-like numbers.
         *
         * Size-like numbers are things which designates sizes, which typically have a small(ish) value. Therefore you
         * can usually save a fair bit of space by using a VLQ-like serialization on them.
         *
         * @param size Serializer to use for sizes.
         * @return This builder.
         */
        public Builder size(Serializer<Integer> size) {
            if (size == null) {
                throw new NullPointerException("containerSize");
            }

            this.size = size;
            return this;
        }

        /**
         * Set serializer to use for sub-type ids.
         *
         * @param subTypeId Serializer to use for sub-type ids.
         * @return This builder.
         */
        public Builder subTypeId(Serializer<Integer> subTypeId) {
            if (subTypeId == null) {
                throw new NullPointerException("subTypeId");
            }

            this.subTypeId = subTypeId;
            return this;
        }

        /**
         * Set serializer to use for enum ordinal values.
         *
         * @param subTypeId Serializer to use for enum ordinal values.
         * @return This builder.
         */
        public Builder enumOrdinal(Serializer<Integer> enumOrdinal) {
            if (enumOrdinal == null) {
                throw new NullPointerException("enumOrdinal");
            }

            this.enumOrdinal = enumOrdinal;
            return this;
        }

        /**
         * Set serializer to use for string sizes.
         *
         * @param subTypeId Serializer to use for string sizes.
         * @return This builder.
         */
        public Builder stringSize(Serializer<Integer> stringSize) {
            if (stringSize == null) {
                throw new NullPointerException("stringSize");
            }

            this.stringSize = stringSize;
            return this;
        }

        public Builder defaultLengthPolicy(LengthPolicy defaultLengthPolicy) {
            if (defaultLengthPolicy == null) {
                throw new NullPointerException("defaultLengthPolicy");
            }

            this.defaultLengthPolicy = defaultLengthPolicy;
            return this;
        }

        public TinySerializer build() {
            final Serializer<Integer> size = ofNullable(this.size).orElseGet(this::defaultCollectionSize);
            final Serializer<Integer> subTypeId = ofNullable(this.subTypeId).orElse(size);
            final Serializer<Integer> enumOrdinal = ofNullable(this.enumOrdinal).orElse(size);
            final LengthPolicy defaultLengthPolicy = ofNullable(this.defaultLengthPolicy).orElse(DEFAULT_LENGTH_POLICY);

            final Serializer<byte[]> byteArray = ofNullable(this.byteArray).orElseGet(defaultByteArray(size));
            final Serializer<char[]> charArray = ofNullable(this.charArray).orElseGet(defaultCharArray(size));
            final Serializer<String> string = ofNullable(this.string)
                    .orElseGet(defaultString(ofNullable(this.stringSize).orElse(size)));

            final Serializer<Boolean> bool = ofNullable(this.bool).orElseGet(() -> new BooleanSerializer());
            final Serializer<Short> shortNumber = ofNullable(this.shortNumber).orElseGet(() -> new ShortSerializer());
            final Serializer<Integer> integer = ofNullable(this.integer).orElseGet(() -> new IntegerSerializer());
            final Serializer<Long> longNumber = ofNullable(this.longNumber).orElseGet(() -> new LongSerializer());
            final Serializer<Float> floatNumber = ofNullable(this.floatNumber)
                    .orElseGet(() -> new FloatSerializer(integer));
            final Serializer<Double> doubleNumber = ofNullable(this.doubleNumber)
                    .orElseGet(() -> new DoubleSerializer(longNumber));

            final Serializer<Integer> varint = ofNullable(this.varint).orElseGet(this::defaultVarInt);
            final Serializer<Long> varlong = ofNullable(this.varlong).orElseGet(this::defaultVarLong);
            final Serializer<UUID> uuid = ofNullable(this.uuid).orElseGet(this.defaultUUID(longNumber));

            return new TinySerializer(size, subTypeId, enumOrdinal, defaultLengthPolicy, byteArray, charArray, string,
                    bool, shortNumber, integer, longNumber, floatNumber, doubleNumber, varint, varlong, uuid,
                    useStringsForEnums);
        }

        private Serializer<Integer> defaultCollectionSize() {
            if (useCompactSize) {
                return defaultVarInt();
            }

            return new IntegerSerializer();
        }

        private Supplier<Serializer<byte[]>> defaultByteArray(Serializer<Integer> size) {
            return () -> new ByteArraySerializer(size);
        }

        private Supplier<? extends Serializer<char[]>> defaultCharArray(Serializer<Integer> size) {
            return () -> new CharArraySerializer(size);
        }

        private Supplier<Serializer<String>> defaultString(Serializer<Integer> size) {
            return () -> new StringSerializer(size);
        }

        private Serializer<Integer> defaultVarInt() {
            if (useSimplerVariableLength) {
                return new CompactVarIntSerializer();
            }

            return new VarIntSerializer();
        }

        private Serializer<Long> defaultVarLong() {
            if (useSimplerVariableLength) {
                return new CompactVarLongSerializer();
            }

            return new VarLongSerializer();
        }

        private Supplier<Serializer<UUID>> defaultUUID(Serializer<Long> longNumber) {
            return () ->  new UUIDSerializer(longNumber);
        }
    }
}
