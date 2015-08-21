# TinySerializer

A small serialization framework for Java.

* Composable serializers with very little fuzz.
* Primitive serializers are external to the the I/O abstraction, making the
  reading and writing very easy to implement safely and efficiently.
* Optionally removes boilerplate through reflection-free annotation processing.
* No static components, the framework uses an instance of `SerializerFramework`
  as its main point of contact.
  See the [Setup](#setup) section.

# Setup

Add tiny-serializer-core as a dependency to your project, and tiny-serializer-api as
a dependency to your public API (if you have any).

```xml
<dependency>
  <groupId>eu.toolchain.serializer</groupId>
  <artifactId>tiny-serializer-core</artifactId>
</dependency>
```

TinySerializer should be setup using the provided builder class.

The easiest possible setup would look like the following.

```java
TinySerializer.builder().build()
```

There are quite a few configuration options available in the builder.

* `#containerSize(Serializer<Integer> containerSize)` configures the serializer
  to use for contain sizes, this default to `VarIntSerializer`.
* `#subTypeId(Serializer<Integer> containerSize)` configures the serializer to
  use for sub-type id mapping, this defaults to `VarIntSerializer`.
* `#enumOrdinal(Serializer<Integer>)` configures the serializer to use for
  enum ordinal values, this defaults to `VarIntSerializer`.
* `#stringSize(Serializer<Integer>)` configures the serializer to use for
  string size prefixes, this defaults to `VarIntSerializer`.

Examples:

* [Serializer Setup](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializerSetup.java)

# Performance

Due to the very explicit nature of TinySerializer, you can expect it thrive
when it comes to serialization performance.

Included in this project is a performance testing module built using JMH, it
can be run by doing the following.

```sh
$ mvn clean package
$ java -jar tiny-serializer-perftests/target/benchmarks.jar -bm sample -bu ns
```

The suite runs tests with the following profiles.

* `#testSerializeToNull` which performs serialization against an [OutputStream
  that has a no-op implementation](tiny-serializer-perftests/src/main/java/eu/toolchain/serializer/perftests/NullOutputStream.java).
* `#testSerializeToMemory` which performs serialization against
  a ByteArrayOutputStream.
* `#testDeserializeFromMemory` which performs deserialization from a previously
  serialized object in-memory.

TinySerializer performance is being compared against the following frameworks.

* [Kryo](https://github.com/EsotericSoftware/kryo)
* [FST](https://github.com/RuedigerMoeller/fast-serialization)

# Usage

The following section contains documentation on how to use TinySerializer.

## Annotation Processing

In order to enable annotation processing, add a dependency to the processor and
api artifact.

```xml
<dependencies>
  <dependency>
    <groupId>eu.toolchain.serializer</groupId>
    <artifactId>tiny-serializer-api</artifactId>
  </dependency>

  <dependency>
    <groupId>eu.toolchain.serializer</groupId>
    <artifactId>tiny-serializer-processor</artifactId>
    <scope>provided</scope>
  </dependency>
<dependencies>
```

Any class that you annotate with `@AutoSerialize` should now have a serializer
built for it.

The processor will only process top-level classes, and the produced serializers
will be named `<Original>_Serializer`.

For many examples, see [annotation processing test
cases](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor)

## Basic serialization

At the core all serializers implement `Serializer<T>`, where `T` is the
type of the value being serialized.

Serialization happens through two methods,
`Serializer#serialize(SerialWriter, T)`, and
`T Serializer#deserialzie(SerialReader)`.

The instance of a `Serializer` must be completely stateless, and therefore
thread-safe.

`SerialReader` and `SerialWriter` are the two primary I/O interfaces,
they perform reading and writing respectively.

The bundled implementations of these can be found in [`eu.toolchain.serializer.io`](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io).

How to implement your own is documented further down in [Implementing a Writer/Reader](#implementing-a-writer-reader).

## Serializing primitive values

The following methods give access to primitive serializers.

* `Serializer<Boolean> #bool()`
* `Serializer<Short> #shortNumber()`
* `Serializer<Integer> #integer()`
* `Serializer<Long> #longNumber()`
* `Serializer<Float> #floatNumber()`
* `Serializer<Double> #doubleNumber()`

Other serializers include.

* `Serializer<String> #string(Serializer<T>)`
* `Serializer<List<T>> #list(Serializer<T>)`
* `Serializer<Map<K, V>> #map(Serializer<K>, Serializer<V>)`
* `Serializer<SortedMap<K, V>> #sortedMap(Serializer<K>, Serializer<V>)`
* `Serializer<Set<T>> #set(Serializer<T>)`
* `Serializer<SortedSet<T>> #sortedSet(Serializer<T>)`

`byte` is omitted since it is part of the I/O abstraction.

`char` is omitted since I haven't had use for it yet, they typically come
in strings for which there are `TinySerializer#string()`.

Examples:

* [Primitive Example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializePrimitiveExample.java)

## Implementing a Serial{Reader|Writer}

`SerialReader` and `SerialWriter` are the two primary I/O interfaces, they perform reading and writing respectively.

To implement a custom `SerialWriter`, use the available [`AbstractSerialWriter`](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io/AbstractSerialWriter.java) as a base.

To implement a custom `SerialReader`, use the available [`AbstractSerialReader`](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io/AbstractSerialReader.java) as a base.

Examples:

* [Custom reader example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeCustomReaderExample.java)
* [Custom writer example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeCustomWriterExample.java)
