package eu.toolchain.serializer;

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

/**
 * Serialization Framework.
 * <p>
 * This type is the entry-point to everything regarding serialization.
 * <p>
 * <h1>Fixed vs. Variable Width Serializers</h1>
 * <p>
 * A fixed-width {@link Serializer} always marshalls the value into the same number of bytes.
 * <p>
 * A variable-width {@link Serializer} encodes the value using a varying number of bytes, typically
 * to reduce the size for more frequent numerals.
 *
 * @author udoprog
 */
public interface SerializerFramework {
  /**
   * A fixed-length {@link Serializer} for {@link String}s.
   */
  public Serializer<String> string();

  /**
   * A fixed-length {@link Serializer} for {@link Byte}s.
   */
  public Serializer<Byte> fixedByte();

  /**
   * A fixed-length {@link Serializer} for {@link Boolean}s.
   */
  public Serializer<Boolean> fixedBoolean();

  /**
   * A fixed-length {@link Serializer} for {@link Short}s.
   */
  public Serializer<Short> fixedShort();

  /**
   * A fixed-length {@code Serializer} for {@link Integer}s.
   */
  public Serializer<Integer> fixedInteger();

  /**
   * A fixed-length {@link Serializer} for {@link Long}s.
   */
  public Serializer<Long> fixedLong();

  /**
   * A fixed-length {@link Serializer} for {@link Float}s.
   */
  public Serializer<Float> fixedFloat();

  /**
   * A fixed-length {@link Serializer} for {@link Double}s.
   */
  public Serializer<Double> fixedDouble();

  /**
   * A fixed-length {@link Serializer} for {@link Character}s.
   */
  public Serializer<Character> fixedCharacter();

  /**
   * A variable-length {@link Serializer} for {@link Integer}s.
   */
  public Serializer<Integer> variableInteger();

  /**
   * A variable-length {@link Serializer} for {@link Long}s.
   */
  public Serializer<Long> variableLong();

    /* deprecated */

  /**
   * @see #fixedBoolean()
   */
  @Deprecated
  public Serializer<Boolean> bool();

  /**
   * @see #fixedShort()
   */
  @Deprecated
  public Serializer<Short> shortNumber();

  /**
   * @see #fixedInteger()
   */
  @Deprecated
  public Serializer<Integer> integer();

  /**
   * @see #fixedLong()
   */
  @Deprecated
  public Serializer<Long> longNumber();

  /**
   * @see #fixedFloat()
   */
  @Deprecated
  public Serializer<Float> floatNumber();

  /**
   * @see #fixedDouble()
   */
  @Deprecated
  public Serializer<Double> doubleNumber();

  /**
   * @see #variableInteger()
   */
  @Deprecated
  public Serializer<Integer> varint();

  /**
   * @see #variableLong()
   */
  @Deprecated
  public Serializer<Long> varlong();

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
   * <p>
   * If this prefix is missing when reading, a runtime exception will be thrown.
   *
   * @param prefix The prefix to use.
   * @param serializer The {@code Serializer} to prefix.
   * @return A new {@code Serializer} with the prefix operation.
   */
  public <T> Serializer<T> prefix(byte[] prefix, Serializer<T> serializer);

  /**
   * Create a length-prefixed serialized that buffers up data, and guarantees that the underlying
   * child-serializer gets the specified amount.
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
   * <p>
   * How the list is encoded is implementation specific.
   *
   * @param <T> Type of the list items.
   * @param serializer The {@code Serializer} to use for each list item.
   * @return A new list {@code Serializer}.
   */
  public <T> Serializer<List<T>> list(Serializer<T> serializer);

  /**
   * Build a {@code Serializer} that can serialize a map.
   * <p>
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
   * <p>
   * How the map is encoded is implementation specific.
   *
   * @param <T> Type of map keys.
   * @param <V> Type of map values.
   * @param key The {@code Serializer} to use for map keys.
   * @param value The {@code Serializer} to use for map values.
   * @return A new map {@code Serializer}.
   */
  public <K extends Comparable<?>, V> Serializer<SortedMap<K, V>> sortedMap(
    Serializer<K> key, Serializer<V> value
  );

  public <K extends Comparable<?>, V> Serializer<NavigableMap<K, V>> navigableMap(
    Serializer<K> key, Serializer<V> value
  );

  /**
   * Build a {@code Serializer} that can serialize a set.
   * <p>
   * How the ser is encoded is implementation specific.
   *
   * @param <T> Type of the set items.
   * @param serializer The {@code Serializer} to use for each set item.
   * @return A new set {@code Serializer}.
   */
  public <T> Serializer<Set<T>> set(Serializer<T> serializer);

  /**
   * Build a {@code Serializer} that can serialize a sorted set.
   * <p>
   * How the ser is encoded is implementation specific.
   *
   * @param <T> Type of the set items.
   * @param serializer The {@code Serializer} to use for each set item.
   * @return A new set {@code Serializer}.
   */
  public <T extends Comparable<?>> Serializer<SortedSet<T>> sortedSet(Serializer<T> serializer);

  public <T extends Comparable<?>> Serializer<NavigableSet<T>> navigableSet(
    Serializer<T> serializer
  );

  /**
   * Create a new serializer for the given array.
   *
   * @param element The element serializer to use.
   * @param constructor The array constructor to use.
   * @return A new array serializer.
   */
  public <T> Serializer<T[]> array(Serializer<T> element, ArrayConstructor<T> constructor);

  public Serializer<boolean[]> booleanArray();

  public Serializer<short[]> shortArray();

  public Serializer<int[]> intArray();

  public Serializer<long[]> longArray();

  public Serializer<float[]> floatArray();

  public Serializer<double[]> doubleArray();

  /**
   * A {@code Serializer} that can serialize a byte array.
   */
  public Serializer<byte[]> byteArray();

  /**
   * A {@code Serializer} that can serialize a {@link java.nio.ByteBuffer}.
   */
  public Serializer<ByteBuffer> byteBuffer();

  /**
   * A {@code Serializer} that can serialize a char array.
   */
  public Serializer<char[]> charArray();

  /**
   * A {@code Serializer} that can serializer {@link UUID}s.
   */
  public Serializer<UUID> uuid();

  /**
   * A {@code Serializer} that can serializer {@link BitSet}s.
   */
  public Serializer<BitSet> bitSet();

  /**
   * Build a serializer for an optional type.
   *
   * @param element The element serializer to use.
   * @return A serializer for the specified optional type.
   */
  public <T> Serializer<Optional<T>> optional(Serializer<T> element);

  /**
   * Same as {@link #forEnum(Object[], Object)}, but throws an exception when out-of-range.
   *
   * @see #forEnum(Object[], Object)
   */
  public <T extends Enum<T>> Serializer<T> forEnum(final T[] values);

  /**
   * A serializer that can serialize enums.
   * <p>
   * Example usage:
   * <p>
   * <pre>
   * {@code
   *     // setup a serializer framework
   *     SerializerFramework s = null;
   *     Serializer<MyEnum> myEnumSerializer = s.forEnum(MyEnum.values());
   * }
   * </pre>
   *
   * @param values Enum values.
   * @param defaultValue Default value to use when enum deserialized value is out-of-range.
   * @return A serializer for enum values.
   */
  public <T extends Enum<T>> Serializer<T> forEnum(T[] values, T defaultValue);

  /**
   * A {@code Serializer} that throws a runtime exception with the message "not implemented".
   */
  public <T> Serializer<T> notImplemented();

  /**
   * A type mapping, that maps a specific type {@code Class<K>}, to a {@code Serializer}, and an
   * ordinal id.
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
   * <p>
   * It is not recommended that you use this method when integrating this framework. For that,
   * implement a custom {@code SerialWriter} that fits your needs to the letter instead.
   *
   * @param serializer The {@code Serializer} to use.
   * @param value The value to serialize.
   * @return A ByteBuffer containing the serialized value.
   * @throws IOException If serialization fails.
   */
  public <T> ByteBuffer serialize(Serializer<T> serializer, T value) throws IOException;

  /**
   * Deserialize a buffer.
   * <p>
   * It is not recommended that you use this method when integrating this framework. For that,
   * implement a custom {@code SerialReader} that fits your needs to the letter instead.
   *
   * @param serializer The {@code Serializer} to use.
   * @param buffer The buffer to deserialize.
   * @return The given value.
   * @throws IOException If serialization fails.
   */
  public <T> T deserialize(Serializer<T> serializer, ByteBuffer buffer) throws IOException;

  /**
   * Write directly to the provided byte buffer.
   *
   * @param buffer Byte buffer to write to.
   * @return A new {@link SerialWriter} writing to the provided {@link ByteBuffer}.
   */
  public SerialWriter writeByteBuffer(ByteBuffer buffer);

  /**
   * Read directly from the given ByteBuffer.
   *
   * @param buffer Buffer to read from.
   * @return A new {@link SerialReader} reading directly from the given ByteBuffer.
   */
  public SerialReader readByteBuffer(ByteBuffer buffer);

  public SerialWriter writeByteChannel(WritableByteChannel channel);

  public SerialReader readByteChannel(ReadableByteChannel channel);

  /**
   * Write to a growing byte array buffer.
   *
   * @return A new {@link SerialWriter} capable of growing as its being written to.
   */
  public BytesSerialWriter writeBytes();

  /**
   * Read directly from the given byte array.
   *
   * @param bytes Byte array to read from.
   * @return A new {@link SerialReader} reading from the given byte array.
   */
  public SerialReader readByteArray(byte[] bytes);

  /**
   * Write to the given {@link OutputStream}.
   * <p>
   * This returns a special writer implementation {@link StreamSerialWriter} which provides
   * flushing capabilities. It is important to flush the underlying stream when you wish for any
   * previously serialized objects to be written. Flushing is implied when closing the stream.
   *
   * @param output The output stream to write to.
   * @return A new {@link SerialWriter} connected to the given {@link OutputStream}.
   */
  public StreamSerialWriter writeStream(OutputStream output);

  /**
   * Read from the given {@link InputStream}.
   * <p>
   * Reading from input streams are very likely to block.
   *
   * @param input The input stream to read from.
   * @return A new {@link SerialReader} connected to the given {@link InputStream}.
   */
  public SerialReader readStream(InputStream input);

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
}
