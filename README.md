# TinySerializer

A small serialization framework for Java for Immutable objects.

* Simple, compsable serializer interface with little fuzz.
* Optionally removes boilerplate through reflection-free
  [annotation processing](#annotation-processing).
* No static components, the framework uses an instance of `SerializerFramework`
  as its main contact point.
  See the [Setup](#setup) section.
* Encourages [immutable objects](#immutable-objects).

# Setup

Add `tiny-serializer-core` as a dependency to your project, and
`tiny-serializer-api` as a dependency to your public API (if you have any).

You can find the latest version in [maven
central](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aeu.toolchain.serializer).

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

### Immutable Objects

Immutable objects are notoriously _hard_ to serialize.

Most complications comes out of the fact that it needs to be constructed and
traditionally very little field information is available to correlated with the
available constructor about how this can be achieved.

Consider the following example.

```java
public class SerializableObject {
    final int a;
    final int b;

    public SerializableObject(int c, int b) {
        this.a = b;
        this.b = c;
    }
}
```

We can see that there _is_ a constructor available for this immutable type, but
figuring this out programmatically is a whole other level.

* We have a constructor that matches purely by the field type (`int`), but the
  order in which the field are assigned is _wrong_.
* The parameters have misleading, or non-existing names.

TinySerializer uses two strategies for handling this.

#### Implicit use of Constructor

The default strategy which involves blindly taking all the fields, and mashing
them in the discovered order into the constructor.

This works pretty good because the the annotation processing happens early in
the compiler, causing it to be able to verify that at least a matching
signature _is_ available.

It also meshes very well with lombok's `@RequiredFieldConstructor`.

This approach have a fundamental issue in that the AST provided to the
processor does not guarantee that fields are exposed in the same order at which
they are defined.
This is the case for vanilla _javac_, but not so much for _Eclipse_ (or
potentially other IDE's) making it hard to work with.

To combat this, you can add the `@AutoSerialize.Field(id = <int>)` annotation,
where the default strategy will be to order the fields according to the value
of this field and use the matching constructor.

Examples:

* [Fields Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Fields.java)
  ([serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Fields_Serializer.java))
* [Ordering Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Ordering.java)
  ([serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Ordering_Serializer.java))

#### Builder Types

The serialized object can specify a builder type through
`@AutoSerialize.Builder` for which all field construction will be delegated.

This is very convenient to couple with
[`@AutoMatter`](chttps://github.com/danielnorberg/auto-matter) or lomok's
`@Builder`.

Examples:

* [Builder Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilder.java)
  ([serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilder_Serializer.java))
* [Builder through Constructor Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilderConstructor.java)
  ([serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilderConstructor_Serializer.java))

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

How to implement your own is documented further down in [Implementing a Writer/Reader](#implementing-a-serialreaderwriter).

Examples:

* [Stream Communication](tiny-serializer-core/src/example/java/eu/toolchain/examples/StreamCommunicationExample.java)

## Serializing Values

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
* [Serialize a Map](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeMap.java)

## Implementing a Serial{Reader|Writer}

`SerialReader` and `SerialWriter` are the two primary I/O interfaces, they perform reading and writing respectively.

To implement a custom `SerialWriter`, use the available [`AbstractSerialWriter`](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io/AbstractSerialWriter.java) as a base.

To implement a custom `SerialReader`, use the available [`AbstractSerialReader`](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io/AbstractSerialReader.java) as a base.

Examples:

* [Custom reader example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeCustomReaderExample.java)
* [Custom writer example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeCustomWriterExample.java)
