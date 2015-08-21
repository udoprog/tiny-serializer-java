package eu.toolchain.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

import eu.toolchain.serializer.io.ByteArraySerializer;
import eu.toolchain.serializer.io.ByteBufferSerialReader;
import eu.toolchain.serializer.io.ByteBufferSerialWriter;
import eu.toolchain.serializer.io.CharArraySerializer;

public class TinySerializer implements SerializerFramework {
    /**
     * Create a new TinySerializer instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    private final Serializer<Integer> collectionSize;
    private final Serializer<Integer> subTypeId;
    private final Serializer<Integer> enumOrdinal;
    private final LengthPolicy defaultLengthPolicy;

    private final ByteArraySerializer byteArray;
    private final CharArraySerializer charArray;
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

    private TinySerializer(Serializer<Integer> collectionSize, Serializer<Integer> subTypeId,
            Serializer<Integer> enumOrdinal, Serializer<Integer> stringSize, LengthPolicy defaultLengthPolicy) {
        this.collectionSize = collectionSize;
        this.subTypeId = subTypeId;
        this.enumOrdinal = enumOrdinal;
        this.defaultLengthPolicy = defaultLengthPolicy;

        this.byteArray = new ByteArraySerializer(collectionSize);
        this.charArray = new CharArraySerializer(collectionSize);
        this.string = new StringSerializer(stringSize);

        this.bool = new BooleanSerializer();
        this.shortNumber = new ShortSerializer();
        this.integer = new IntegerSerializer();
        this.longNumber = new LongSerializer();
        this.floatNumber = new FloatSerializer(integer);
        this.doubleNumber = new DoubleSerializer(longNumber);

        this.varint = new CompactVarIntSerializer();
        this.varlong = new VarLongSerializer();
        this.uuid = new UUIDSerializer(longNumber);
    }

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
    public <T> Serializer<T> nullable(final Serializer<T> serializer) {
        return new NullSerializer<T>(serializer);
    }

    @Override
    public <T> Serializer<T> prefix(int prefix, final Serializer<T> serializer) {
        return prefix(ByteBuffer.allocate(Integer.BYTES).putInt(prefix).array(), serializer);
    }

    @Override
    public <T> Serializer<T> prefix(final byte[] prefix, final Serializer<T> serializer) {
        return new PrefixSerializer<T>(prefix, serializer);
    }

    @Override
    public <T> Serializer<T> lengthPrefixed(Serializer<T> serializer) {
        return lengthPrefixed(serializer, defaultLengthPolicy);
    }

    @Override
    public <T> Serializer<T> lengthPrefixed(Serializer<T> serializer, LengthPolicy policy) {
        return new LengthPrefixedSerializer<T>(varlong(), serializer, policy);
    }

    @Override
    public <T> Serializer<List<T>> list(Serializer<T> serializer) {
        return new ListSerializer<T>(collectionSize, serializer);
    }

    @Override
    public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value) {
        return new MapSerializer<K, V>(collectionSize, key, value);
    }

    @Override
    public <K, V> Serializer<SortedMap<K, V>> sortedMap(Serializer<K> key, Serializer<V> value) {
        return new SortedMapSerializer<K, V>(collectionSize, key, value);
    }

    @Override
    public <T> Serializer<Set<T>> set(Serializer<T> serializer) {
        return new SetSerializer<T>(collectionSize, serializer);
    }

    @Override
    public <T> Serializer<SortedSet<T>> sortedSet(Serializer<T> serializer) {
        return new SortedSetSerializer<T>(collectionSize, serializer);
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
        return new EnumSerializer<T>(enumOrdinal, values);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Serializer<T> notImplemented() {
        return (Serializer<T>) notImplemented;
    }

    @Override
    public <T extends S, S> TypeMapping<T, S> type(int id, Class<T> key, Serializer<T> serializer) {
        if (id >= 0xffff || id < 0)
            throw new IllegalArgumentException("id must be a positive number smaller than 0xffff");

        return new TypeMapping<T, S>(id, key, serializer);
    }

    @Override
    public final <T> Serializer<T> subtypes(Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        final Map<Integer, TypeMapping<? extends T, T>> ids = buildIdMapping(mappings);
        final Map<Class<? extends T>, TypeMapping<? extends T, T>> keys = buildTypeMapping(mappings);

        return new Serializer<T>() {
            @Override
            public void serialize(SerialWriter buffer, T value) throws IOException {
                final TypeMapping<? extends T, T> m = keys.get(value.getClass());

                if (m == null) {
                    throw new IllegalArgumentException("Type not supported: " + value.getClass());
                }

                subTypeId.serialize(buffer, m.id());
                @SuppressWarnings("unchecked")
                final Serializer<T> serializer = (Serializer<T>) m.serializer();
                serializer.serialize(buffer, value);
            }

            @Override
            public T deserialize(SerialReader buffer) throws IOException {
                final int id = subTypeId.deserialize(buffer);
                final TypeMapping<? extends T, T> m = ids.get(id);

                if (m == null) {
                    throw new IllegalArgumentException("Type id not supported: " + id);
                }

                return m.serializer().deserialize(buffer);
            }
        };
    }

    private <T> Map<Integer, TypeMapping<? extends T, T>> buildIdMapping(
            Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        final Map<Integer, TypeMapping<? extends T, T>> mapping = new HashMap<>();

        for (TypeMapping<? extends T, T> m : mappings) {
            if (mapping.put(m.id(), m) == null)
                continue;

            throw new IllegalArgumentException("Duplicate mappings for " + m);
        }

        return mapping;
    }

    private <T> Map<Class<? extends T>, TypeMapping<? extends T, T>> buildTypeMapping(
            Iterable<? extends TypeMapping<? extends T, T>> mappings) {
        final Map<Class<? extends T>, TypeMapping<? extends T, T>> mapping = new HashMap<>();

        for (final TypeMapping<? extends T, T> m : mappings) {
            if (mapping.put(m.key(), m) == null)
                continue;

            throw new IllegalArgumentException("Duplicate mappings for " + m);
        }

        return mapping;
    }

    public static final class Builder {
        public static final Serializer<Integer> DEFAULT_INTEGER = new CompactVarIntSerializer();

        private Serializer<Integer> collectionSize = DEFAULT_INTEGER;
        private Serializer<Integer> subTypeId = DEFAULT_INTEGER;
        private Serializer<Integer> enumOrdinal = DEFAULT_INTEGER;
        private Serializer<Integer> stringSize = DEFAULT_INTEGER;
        private LengthPolicy defaultLengthPolicy = new MaxLengthPolicy(Integer.MAX_VALUE);

        /**
         * Set serializer to use for container sizes.
         *
         * @param containerSize Serializer to use for container sizes.
         * @return This builder.
         */
        public Builder collectionSize(Serializer<Integer> containerSize) {
            if (containerSize == null) {
                throw new NullPointerException("containerSize");
            }

            this.collectionSize = containerSize;
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
            return new TinySerializer(collectionSize, subTypeId, enumOrdinal, stringSize, defaultLengthPolicy);
        }
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
}
