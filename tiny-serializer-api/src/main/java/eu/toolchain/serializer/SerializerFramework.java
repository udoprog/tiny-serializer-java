package eu.toolchain.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

public interface SerializerFramework {
    /**
     * A {@code Serializer} for strings.
     */
    public Serializer<String> string();

    /**
     * A {@code Serializer} for booleans.
     */
    public Serializer<Boolean> bool();

    /**
     * A {@code Serializer} for integers that uses a variable length encoding that is more space-efficient at encoding
     * smaller values than the naive approach.
     */
    public Serializer<Integer> varint();

    /**
     * A {@code Serializer} for longs that uses a variable length encoding that is more space-efficient at encoding
     * smaller values than the naive approach.
     */
    public Serializer<Long> varlong();

    /**
     * A {@code Serializer} for 16 bit signed integers (short).
     */
    public Serializer<Short> shortNumber();

    /**
     * A {@code Serializer} for 32 bit signed integers (int).
     */
    public Serializer<Integer> integer();

    /**
     * A {@code Serializer} for 64 bit signed integers (long)..
     *
     * @return
     */
    public Serializer<Long> longNumber();

    /**
     * A {@code Serializer} for 32 bit floating point numbers (float).
     */
    public Serializer<Float> floatNumber();

    /**
     * A {@code Serializer} for 64 bit floating point numbers (double).
     */
    public Serializer<Double> doubleNumber();

    /* more fancy things */

    /**
     * Build a {@code Serializer} that can serialize null values.
     */
    public <T> Serializer<T> nullable(Serializer<T> serializer);

    /**
     * Same as {@link #prefix(byte[], Serializer)}, but using a 32 bit integer as a prefix.
     */
    public <T> Serializer<T> prefix(int prefix, Serializer<T> serializer);

    /**
     * Build a {@code Serializer} that adds the specified prefix when serializing.
     *
     * If this prefix is missing when reading, a runtime exception will be thrown.
     *
     * @param prefix The prefix to use.
     * @param serializer The {@code Serializer} to prefix.
     * @return A new {@code Serializer} with the prefix operation.
     */
    public <T> Serializer<T> prefix(byte[] prefix, Serializer<T> serializer);

    /**
     * Create a length-prefixed serialized that buffers up data, and guarantees that the underlying child-serializer
     * gets the specified amount.
     *
     * @param serializer The {@code Serializer} to length prefix.
     * @param policy A policy governing how long a child message is allowed to be.
     * @return A new {@code Serializer} with the length prefix operation.
     */
    public <T> Serializer<T> lengthPrefixed(Serializer<T> serializer, LengthPolicy policy);

    /**
     * This will use a the default length policy that is configured in the framework.
     *
     * @see #lengthPrefixed(Serializer, LengthPolicy)
     */
    public <T> Serializer<T> lengthPrefixed(Serializer<T> serializer);

    /**
     * Build a {@code Serializer} that can serialize a list.
     *
     * How the list is encoded is implementation specific.
     *
     * @param <T> Type of the list items.
     * @param serializer The {@code Serializer} to use for each list item.
     * @return A new list {@code Serializer}.
     */
    public <T> Serializer<List<T>> list(Serializer<T> serializer);

    /**
     * Build a {@code Serializer} that can serialize a map.
     *
     * How the map is encoded is implementation specific.
     *
     * @param <T> Type of map keys.
     * @param <V> Type of map values.
     * @param key The {@code Serializer} to use for map keys.
     * @param value The {@code Serializer} to use for map values.
     * @return A new map {@code Serializer}.
     */
    public <K, V> Serializer<Map<K, V>> map(Serializer<K> key, Serializer<V> value);

    /**
     * Build a {@code Serializer} that can serialize a sorted map.
     *
     * How the map is encoded is implementation specific.
     *
     * @param <T> Type of map keys.
     * @param <V> Type of map values.
     * @param key The {@code Serializer} to use for map keys.
     * @param value The {@code Serializer} to use for map values.
     * @return A new map {@code Serializer}.
     */
    public <K, V> Serializer<SortedMap<K, V>> sortedMap(Serializer<K> key, Serializer<V> value);

    /**
     * Build a {@code Serializer} that can serialize a set.
     *
     * How the ser is encoded is implementation specific.
     *
     * @param <T> Type of the set items.
     * @param serializer The {@code Serializer} to use for each set item.
     * @return A new set {@code Serializer}.
     */
    public <T> Serializer<Set<T>> set(Serializer<T> serializer);

    /**
     * Build a {@code Serializer} that can serialize a sorted set.
     *
     * How the ser is encoded is implementation specific.
     *
     * @param <T> Type of the set items.
     * @param serializer The {@code Serializer} to use for each set item.
     * @return A new set {@code Serializer}.
     */
    public <T> Serializer<SortedSet<T>> sortedSet(Serializer<T> serializer);

    /**
     * A {@code Serializer} that can serialize a byte array.
     */
    public Serializer<byte[]> byteArray();

    /**
     * A {@code Serializer} that can serialize a char array.
     */
    public Serializer<char[]> charArray();

    /**
     * A {@code Serializer} that can serializer UUIDs.
     */
    public Serializer<UUID> uuid();

    /**
     * Build a serializer for an optional type.
     *
     * @param element The element serializer to use.
     * @return A serializer for the specified optional type.
     */
    public <T> Serializer<Optional<T>> optional(Serializer<T> element);

    // @formatter:off
    /**
     * A serializer that can serialize enums.
     *
     * Example usage:
     *
     * <pre>
     * {@code
     *     // setup a serializer framework
     *     SerializerFramework s = null;
     *     Serializer<MyEnum> myEnumSerializer = s.forEnum(MyEnum.values());
     * }
     * </pre>
     *
     * @param values
     * @return
     */
    // @formatter:on
    public <T extends Enum<T>> Serializer<T> forEnum(final T[] values);

    /**
     * A {@code Serializer} that throws a runtime exception with the message "not implemented".
     */
    public <T> Serializer<T> notImplemented();

    /**
     * Maps a specific ordinal id to a type (key), and a serializer for that type.
     *
     * @param <T> The mapped type.
     */
    public static class TypeMapping<T extends S, S> {
        final int id;
        final Class<T> key;
        final Serializer<T> serializer;

        public TypeMapping(int id, Class<T> key, Serializer<T> serializer) {
            this.id = id;
            this.key = key;
            this.serializer = serializer;
        }

        public int id() {
            return id;
        }

        public Class<T> key() {
            return key;
        }

        public Serializer<T> serializer() {
            return serializer;
        }
    }

    /**
     * A type mapping, that maps a specific type {@code Class<K>}, to a {@code Serializer}, and an ordinal id.
     *
     * @param <T> Super type to create mapping for.
     * @param <K> Subtype of the super type <T>.
     * @param id The id of the mapped type.
     * @param key The type of the mapped type.
     * @param serializer The serializer for the mapped type.
     * @return A new typemapping.
     */
    public <T extends S, S> TypeMapping<T, S> type(int id, Class<T> type, Serializer<T> serializer);

    /**
     * Create a {@code Serializer} for the given type mappings.
     *
     * @param <T> The super type that is to be serialized to and from.
     * @param mappings Type mappings.
     * @return A new serializer for the given type mappings.
     */
    public <T> Serializer<T> subtypes(Iterable<? extends TypeMapping<? extends T, T>> mappings);

    /**
     * Create a singleton serializer that always deserializes to the same reference.
     *
     * @param value Reference to serialize.
     * @return The singleton reference.
     */
    public <T> Serializer<T> singleton(T value);

    /**
     * Serialize a value.
     *
     * It is not recommended that you use this method when integrating this framework. For that, implement a custom
     * {@code SerialWriter} that fits your needs to the letter instead.
     *
     * @param serializer The {@code Serializer} to use.
     * @param value The value to serialize.
     * @return A ByteBuffer containing the serialized value.
     * @throws IOException If serialization fails.
     */
    public <T> ByteBuffer serialize(Serializer<T> serializer, T value) throws IOException;

    /**
     * Deserialize a buffer.
     *
     * It is not recommended that you use this method when integrating this framework. For that, implement a custom
     * {@code SerialReader} that fits your needs to the letter instead.
     *
     * @param serializer The {@code Serializer} to use.
     * @param buffer The buffer to deserialize.
     * @return The given value.
     * @throws IOException If serialization fails.
     */
    public <T> T deserialize(Serializer<T> serializer, ByteBuffer buffer) throws IOException;
}
