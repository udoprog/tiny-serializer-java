package eu.toolchain.serializer;

import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Supplier;

import eu.toolchain.serializer.array.ArraySerializer;
import eu.toolchain.serializer.array.BooleanArraySerializer;
import eu.toolchain.serializer.array.ByteArraySerializer;
import eu.toolchain.serializer.array.CharArraySerializer;
import eu.toolchain.serializer.array.DoubleArraySerializer;
import eu.toolchain.serializer.array.FloatArraySerializer;
import eu.toolchain.serializer.array.IntegerArraySerializer;
import eu.toolchain.serializer.array.LongArraySerializer;
import eu.toolchain.serializer.array.ShortArraySerializer;
import eu.toolchain.serializer.io.ContiniousSharedPool;
import eu.toolchain.serializer.io.CoreByteArraySerialReader;
import eu.toolchain.serializer.io.CoreByteBufferSerialReader;
import eu.toolchain.serializer.io.CoreByteBufferSerialWriter;
import eu.toolchain.serializer.io.CoreByteChannelSerialReader;
import eu.toolchain.serializer.io.CoreByteChannelSerialWriter;
import eu.toolchain.serializer.io.CoreBytesSerialWriter;
import eu.toolchain.serializer.io.CoreInputStreamSerialReader;
import eu.toolchain.serializer.io.CoreOutputStreamSerialWriter;
import eu.toolchain.serializer.io.ImmediateSharedPool;
import eu.toolchain.serializer.primitive.BooleanSerializer;
import eu.toolchain.serializer.primitive.ByteSerializer;
import eu.toolchain.serializer.primitive.CharacterSerializer;
import eu.toolchain.serializer.primitive.CompactVarIntSerializer;
import eu.toolchain.serializer.primitive.CompactVarLongSerializer;
import eu.toolchain.serializer.primitive.DoubleSerializer;
import eu.toolchain.serializer.primitive.FloatSerializer;
import eu.toolchain.serializer.primitive.IntegerSerializer;
import eu.toolchain.serializer.primitive.LongSerializer;
import eu.toolchain.serializer.primitive.ShortSerializer;
import eu.toolchain.serializer.primitive.VarIntSerializer;
import eu.toolchain.serializer.primitive.VarLongSerializer;
import eu.toolchain.serializer.type.BitSetSerializer;
import eu.toolchain.serializer.type.LengthPrefixedSerializer;
import eu.toolchain.serializer.type.NullSerializer;
import eu.toolchain.serializer.type.OptionalSerializer;
import eu.toolchain.serializer.type.OrdinalEnumSerializer;
import eu.toolchain.serializer.type.PrefixSerializer;
import eu.toolchain.serializer.type.SingletonSerializer;
import eu.toolchain.serializer.type.StringEnumSerializer;
import eu.toolchain.serializer.type.StringSerializer;
import eu.toolchain.serializer.type.SubTypesSerializer;
import eu.toolchain.serializer.type.UUIDSerializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TinySerializer extends AbstractSerializerFramework {
    public static final Serializer<Integer> DEFAULT_INTEGER = new CompactVarIntSerializer();
    public static final LengthPolicy DEFAULT_LENGTH_POLICY = new MaxLengthPolicy(Integer.MAX_VALUE);

    private final Supplier<SharedPool> pool;

    private final Serializer<Integer> arraySize;
    private final Serializer<Integer> scopeSize;
    private final Serializer<Integer> subTypeId;
    private final Serializer<Integer> enumOrdinal;

    private final LengthPolicy defaultLengthPolicy;

    private final Serializer<boolean[]> booleanArray;
    private final Serializer<short[]> shortArray;
    private final Serializer<int[]> intArray;
    private final Serializer<long[]> longArray;

    private final Serializer<float[]> floatArray;
    private final Serializer<double[]> doubleArray;

    private final Serializer<byte[]> byteArray;
    private final Serializer<char[]> charArray;

    private final Serializer<String> string;

    private final Serializer<Byte> fixedByte;
    private final Serializer<Character> fixedCharacter;
    private final Serializer<Boolean> fixedBoolean;
    private final Serializer<Short> fixedShort;
    private final Serializer<Integer> fixedInteger;
    private final Serializer<Long> fixedLong;
    private final Serializer<Float> fixedFloat;
    private final Serializer<Double> fixedDouble;

    private final Serializer<Integer> variableInteger;
    private final Serializer<Long> variableLong;

    private final Serializer<UUID> uuid;
    private final Serializer<BitSet> bitSet;

    private final CollectionsProvider collections;

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
    public Serializer<Byte> fixedByte() {
        return fixedByte;
    }

    @Override
    public Serializer<Character> fixedCharacter() {
        return fixedCharacter;
    }

    @Override
    public Serializer<Boolean> fixedBoolean() {
        return fixedBoolean;
    }

    @Override
    public Serializer<Integer> fixedInteger() {
        return fixedInteger;
    }

    @Override
    public Serializer<Short> fixedShort() {
        return fixedShort;
    }

    @Override
    public Serializer<Long> fixedLong() {
        return fixedLong;
    }

    @Override
    public Serializer<Float> fixedFloat() {
        return fixedFloat;
    }

    @Override
    public Serializer<Double> fixedDouble() {
        return fixedDouble;
    }

    @Override
    public Serializer<Integer> variableInteger() {
        return variableInteger;
    }

    @Override
    public Serializer<Long> variableLong() {
        return variableLong;
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
        return new LengthPrefixedSerializer<T>(this, variableInteger(), serializer, policy);
    }

    @Override
    public <T> Serializer<List<T>> list(Serializer<T> serializer) {
        return collections.list(serializer);
    }

    @Override
    public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value) {
        return collections.map(key, value);
    }

    @Override
    public <K extends Comparable<?>, V> Serializer<SortedMap<K, V>> sortedMap(Serializer<K> key, Serializer<V> value) {
        return collections.sortedMap(key, value);
    }

    @Override
    public <K extends Comparable<?>, V> Serializer<NavigableMap<K, V>> navigableMap(Serializer<K> key, Serializer<V> value) {
        return collections.navigableMap(key, value);
    }

    @Override
    public <T> Serializer<Set<T>> set(Serializer<T> serializer) {
        return collections.set(serializer);
    }

    @Override
    public <T extends Comparable<?>> Serializer<SortedSet<T>> sortedSet(Serializer<T> serializer) {
        return collections.sortedSet(serializer);
    }

    @Override
    public <T extends Comparable<?>> Serializer<NavigableSet<T>> navigableSet(Serializer<T> serializer) {
        return collections.navigableSet(serializer);
    }

    @Override
    public <T> Serializer<T[]> array(Serializer<T> element, ArrayConstructor<T> constructor) {
        return new ArraySerializer<T>(arraySize, element, constructor);
    }

    @Override
    public Serializer<boolean[]> booleanArray() {
        return booleanArray;
    }

    @Override
    public Serializer<short[]> shortArray() {
        return shortArray;
    }

    @Override
    public Serializer<int[]> intArray() {
        return intArray;
    }

    @Override
    public Serializer<long[]> longArray() {
        return longArray;
    }

    @Override
    public Serializer<float[]> floatArray() {
        return floatArray;
    }

    @Override
    public Serializer<double[]> doubleArray() {
        return doubleArray;
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
    public Serializer<BitSet> bitSet() {
        return bitSet;
    }

    @Override
    public <T> Serializer<Optional<T>> optional(Serializer<T> element) {
        return new OptionalSerializer<T>(fixedBoolean, element);
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
    public <T> ByteBuffer serialize(final Serializer<T> serializer, final T value) throws IOException {
        try (final BytesSerialWriter writer = writeBytes()) {
            serializer.serialize(writer, value);
            return writer.toByteBuffer();
        }
    }

    @Override
    public <T> T deserialize(final Serializer<T> serializer, final ByteBuffer buffer) throws IOException {
        try (final SerialReader reader = readByteBuffer(buffer)) {
            return serializer.deserialize(reader);
        }
    }

    @Override
    public SerialReader readByteBuffer(final ByteBuffer buffer) {
        return new CoreByteBufferSerialReader(pool.get(), scopeSize, buffer);
    }

    @Override
    public SerialWriter writeByteBuffer(ByteBuffer buffer) {
        return new CoreByteBufferSerialWriter(pool.get(), scopeSize, buffer);
    }

    @Override
    public BytesSerialWriter writeBytes() {
        return new CoreBytesSerialWriter(pool.get(), scopeSize);
    }

    @Override
    public SerialReader readByteArray(final byte[] bytes) {
        return new CoreByteArraySerialReader(pool.get(), scopeSize, bytes);
    }

    @Override
    public StreamSerialWriter writeStream(final OutputStream output) {
        return new CoreOutputStreamSerialWriter(pool.get(), scopeSize, output);
    }

    @Override
    public SerialReader readStream(final InputStream input) {
        return new CoreInputStreamSerialReader(pool.get(), scopeSize, input);
    }

    @Override
    public SerialWriter writeByteChannel(WritableByteChannel channel) {
        return new CoreByteChannelSerialWriter(pool.get(), scopeSize, channel);
    }

    @Override
    public SerialReader readByteChannel(ReadableByteChannel channel) {
        return new CoreByteChannelSerialReader(pool.get(), scopeSize, channel);
    }

    /**
     * Create a new TinySerializer instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean immediateSharedPool;

        private Serializer<Integer> size;
        private Serializer<Integer> arraySize;
        private Serializer<Integer> scopeSize;
        private Serializer<Integer> subTypeId;
        private Serializer<Integer> enumOrdinal;
        private LengthPolicy defaultLengthPolicy;

        private Serializer<byte[]> byteArray;
        private Serializer<char[]> charArray;
        private Serializer<String> string;
        private Serializer<Integer> stringSize;

        private Serializer<Character> fixedCharacter;
        private Serializer<Byte> fixedByte;
        private Serializer<Boolean> fixedBoolean;
        private Serializer<Short> fixedShort;
        private Serializer<Integer> integer;
        private Serializer<Long> fixedLong;
        private Serializer<Float> floatNumber;
        private Serializer<Double> doubleNumber;

        private Serializer<Integer> varint;
        private Serializer<Long> varlong;

        private Serializer<UUID> uuid;
        private Serializer<BitSet> bitSet;

        private CollectionsProvider collections;

        private boolean useSimplerVariableLength = false;
        private boolean useCompactSize = true;
        private boolean useStringsForEnums = false;
        private boolean useImmutableCollections = false;

        /**
         * Whether to use an immediate (non pooled) pool implementation or not.
         *
         * @param immediateSharedPool {@code true} if an immediate pool should be used, {@code false} otherwise.
         * @return This builder.
         */
        public Builder immediateSharedPool(boolean immediateSharedPool) {
            this.immediateSharedPool = immediateSharedPool;
            return this;
        }

        /**
         * Prefer the 'simpler' variable length implementation over the more compact one.
         *
         * The simpler is beneficial when you are inspecting traffic by eye, since it performs a less esoteric encoding
         * of the number.
         *
         * @see VarIntSerializer
         * @see CompactVarIntSerializer
         * @param useCompactVariableLength
         * @return This builder.
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
         * Use immutable collections when deserializing data. Note that this does not enforce that serialized
         * collections are immutable, only that the ones produced by the framework are.
         *
         * <b>This option requires that google guava</b> is in your classpath, if this is not the case, an
         * {@link IllegalStateException} will be thrown in the {@link #build()} method call.
         *
         * @param useImmutableCollections {@code true} will cause produced collections to be immutable.
         * @return This builder.
         */
        public Builder useImmutableCollections(boolean useImmutableCollections) {
            this.useImmutableCollections = useImmutableCollections;
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
                throw new NullPointerException("size");
            }

            this.size = size;
            return this;
        }

        /**
         * Configure serializer to use for array sizes.
         *
         * @param arraySize Scope size serializer to use.
         * @return This builder.
         */
        public Builder arraySize(Serializer<Integer> arraySize) {
            if (arraySize == null) {
                throw new NullPointerException("arraySize");
            }

            this.arraySize = arraySize;
            return this;
        }

        /**
         * Configure serializer to use for scope sizes.
         *
         * @param scopeSize Scope size serializer to use.
         * @return This builder.
         */
        public Builder scopeSize(Serializer<Integer> scopeSize) {
            if (scopeSize == null) {
                throw new NullPointerException("scopeSize");
            }

            this.scopeSize = scopeSize;
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

        /**
         * Set a default length policy for the {@link SerializerFramework#lengthPrefixed(Serializer)} serialization.
         *
         * @param defaultLengthPolicy New length policy to set.
         * @return This builder.
         */
        public Builder defaultLengthPolicy(LengthPolicy defaultLengthPolicy) {
            if (defaultLengthPolicy == null) {
                throw new NullPointerException("defaultLengthPolicy");
            }

            this.defaultLengthPolicy = defaultLengthPolicy;
            return this;
        }

        public Builder collections(CollectionsProvider collections) {
            if (collections == null) {
                throw new NullPointerException("collections");
            }

            this.collections = collections;
            return this;
        }

        /**
         * Build a new instance of the Tiny implementation of the {@code SerializerFramework} according to the current
         * configuration.
         *
         * The builder may be modified after an invocation to build.
         *
         * @return
         * @throws IllegalStateException If the configuration is invalid, or the environment does not match the specified configuration.
         * @see #useImmutableCollections
         */
        public TinySerializer build() {
            final Supplier<SharedPool> pool = buildPool();

            final Serializer<Integer> size = ofNullable(this.size).orElseGet(this::defaultCollectionSize);
            final Serializer<Integer> arraySize = ofNullable(this.arraySize).orElse(size);
            final Serializer<Integer> scopeSize = ofNullable(this.scopeSize).orElse(size);
            final Serializer<Integer> subTypeId = ofNullable(this.subTypeId).orElse(size);
            final Serializer<Integer> enumOrdinal = ofNullable(this.enumOrdinal).orElse(size);
            final LengthPolicy defaultLengthPolicy = ofNullable(this.defaultLengthPolicy).orElse(DEFAULT_LENGTH_POLICY);

            final Serializer<boolean[]> booleanArray = new BooleanArraySerializer(arraySize);
            final Serializer<short[]> shortArray = new ShortArraySerializer(arraySize);
            final Serializer<int[]> intArray = new IntegerArraySerializer(arraySize);
            final Serializer<long[]> longArray = new LongArraySerializer(arraySize);

            final Serializer<float[]> floatArray = new FloatArraySerializer(arraySize);
            final Serializer<double[]> doubleArray = new DoubleArraySerializer(arraySize);

            final Serializer<byte[]> byteArray = ofNullable(this.byteArray).orElseGet(defaultByteArray(size));
            final Serializer<char[]> charArray = ofNullable(this.charArray).orElseGet(defaultCharArray(size));

            final Serializer<String> string = ofNullable(this.string)
                    .orElseGet(defaultString(ofNullable(this.stringSize).orElse(size)));

            final Serializer<Byte> fixedByte = ofNullable(this.fixedByte).orElseGet(ByteSerializer::new);
            final Serializer<Character> fixedCharacter = ofNullable(this.fixedCharacter)
                    .orElseGet(CharacterSerializer::new);
            final Serializer<Boolean> fixedBoolean = ofNullable(this.fixedBoolean).orElseGet(BooleanSerializer::new);
            final Serializer<Short> fixedShort = ofNullable(this.fixedShort).orElseGet(ShortSerializer::new);
            final Serializer<Integer> fixedInteger = ofNullable(this.integer).orElseGet(IntegerSerializer::new);
            final Serializer<Long> fixedLong = ofNullable(this.fixedLong).orElseGet(LongSerializer::new);
            final Serializer<Float> fixedFloat = ofNullable(this.floatNumber).orElseGet(FloatSerializer::new);
            final Serializer<Double> fixedDouble = ofNullable(this.doubleNumber).orElseGet(DoubleSerializer::new);

            final Serializer<Integer> variableInteger = ofNullable(this.varint).orElseGet(this::defaultVarInt);
            final Serializer<Long> variableLong = ofNullable(this.varlong).orElseGet(this::defaultVarLong);
            final Serializer<UUID> uuid = ofNullable(this.uuid).orElseGet(this.defaultUUID(fixedLong));
            final Serializer<BitSet> bitSet = ofNullable(this.bitSet).orElseGet(BitSetSerializer.supplier(size));

            final CollectionsProvider collections = ofNullable(this.collections).orElseGet(defaultCollections(size));

            return new TinySerializer(pool, arraySize, scopeSize, subTypeId, enumOrdinal, defaultLengthPolicy,
                    booleanArray, shortArray, intArray, longArray, floatArray, doubleArray, byteArray, charArray,
                    string, fixedByte, fixedCharacter, fixedBoolean, fixedShort, fixedInteger, fixedLong, fixedFloat,
                    fixedDouble, variableInteger, variableLong, uuid, bitSet, collections, useStringsForEnums);
        }

        private Supplier<SharedPool> buildPool() {
            if (immediateSharedPool) {
                return () -> new ImmediateSharedPool();
            }

            return () -> new ContiniousSharedPool();
        }

        private Supplier<CollectionsProvider> defaultCollections(final Serializer<Integer> size) {
            return () -> {
                if (useImmutableCollections) {
                    ImmutableCollectionsProvider.verifyGuavaAvailable();
                    return new ImmutableCollectionsProvider(size);
                }

                return new DefaultCollectionsProvider(size);
            };
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
                return new VarIntSerializer();
            }

            return new CompactVarIntSerializer();
        }

        private Serializer<Long> defaultVarLong() {
            if (useSimplerVariableLength) {
                return new VarLongSerializer();
            }

            return new CompactVarLongSerializer();
        }

        private Supplier<Serializer<UUID>> defaultUUID(Serializer<Long> fixedLong) {
            return () ->  new UUIDSerializer(fixedLong);
        }
    }
}