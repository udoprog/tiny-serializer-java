# TinySerializer

A small serialization framework for Java for Immutable objects.

* Simple, compsable serializer interface with little fuzz.
* Optionally removes boilerplate through reflection-free
  [annotation processing](#generated-serializers).
* No static components, the framework uses an instance of `SerializerFramework`
  as its main contact point.
  See the [Setup](#setup) section.
* Encourages [immutable objects](#immutable-objects).

Topics:

* [Setup](#setup)
* [Serialization Format](#serialization-format)
* [Basic Serialization](#basic-serialization)
* [Generated Serializers](#generated-serializers)
* [Performance](#performance)

# Setup

Add `tiny-serializer-core` as a dependency to your project, and
`tiny-serializer-api` as a dependency to your public API (if you have any).

You can find the latest version in [maven
central](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aeu.toolchain.serializer).

```xml
<dependency>
  <groupId>eu.toolchain.serializer</groupId>
  <artifactId>tiny-serializer-core</artifactId>
  <version>${tiny.version}</version>
</dependency>
```

TinySerializer should be setup using the provided builder class.

The easiest possible setup would look like the following.

```java
TinySerializer.builder().build()
```

There are quite a few configuration options available in the builder.

Examples:

* [Serializer Setup](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializerSetup.java)

# Serialization Format

TinySerializer is _not_ intended to be used as a well-established,
general-purpose serialization format.

It provides a framework to build a specific serialization _on top of_.

This can be useful if you have some data in an application that needs to be
serialized to disk, or if you have to components of the same application
needing to communicate over the network (provide the serializer implementation
as a separate library).

Other than that, the project aims that within a major version, same
builder configuration will generate the same wire-format.
The builder, and API types will be subject to versioning guarantees as-per
semantic versioning.

Stronger guarantees _might_ be possible in the future by providing
special-purpose serialization implementations of `SerializerFramework`.

As it stands right now, what I've (udoprog) done here is take a pattern I
implement very frequently by hand, and make it as convenient as possible.

# Basic serialization

The simplest possible example would like like the following.

```java
public class Example {
    public static void main(String argv[]) {
        final SerializerFramework f = TinySerializer.builder().build();
        final Serializer<String> string = f.string();

        // helper method
        final byte[] message = f.serialize(string, "Hello");
        System.out.println(f.deserialize(string, message));
    }
}
```

At the core all serializers implement `Serializer<T>`, where `T` is the
type of the value being serialized.

Serialization happens through two methods,
`Serializer#serialize(SerialWriter, T)`, and
`T Serializer#deserialzie(SerialReader)`.

The instance of a `Serializer` _must_ be completely thread-safe, the easiest way to accomplishing this is to make it stateless.

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

# Generated Serializers

This project provides an annotation processor (`tiny-serializer-processor`) intended to make Object graph serialization easier.

The processor looks for classes annotated with `@AutoSerialize`, like the following.

```java
@AutoSerialize
class Person {
    private final String name;
    private final Optional<Job> job;

    public Person(String name, Optional<Job> job) {
        this.name = name;
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public Optional<Job> getJob() {
        return job;
    }
}
```

Note: `Job` is assumed here to be another type annotated with `@AutoSerialize`.

Which in turn would generated a serializer named `Person_Serializer`, like the following.

```java
@AutoSerialize
class Person_Serializer implements Serializer<Person> {
    private final Serializer<String> s_String;
    private final Serializer<Optional<Job>> s_OptionalJob;

    public Person_Serializer(final SerializerFramework framework) {
        s_String = framework.string();
        s_OptionalJob = framework.optional(new Job_Serializer(framework));
    }

    public void serialize(SerialWriter buffer, Person person) {
        s_String.serialize(buffer, person.getName());
        s_OptionalJob.serialize(buffer, person.getJob());
    }

    public Person deserialize(SerialWriter buffer) {
        final String v_name = s_String.deserialize(buffer);
        final Optional<Job> v_job = s_OptionalJob.deserialize(buffer);
        return new Person(v_name, v_job);
    }
}
```

### Usage

In order to enable annotation processing, add a dependency to the processor and
api artifact.

```xml
<dependencies>
  <dependency>
    <groupId>eu.toolchain.serializer</groupId>
    <artifactId>tiny-serializer-api</artifactId>
    <version>${tiny.version}</version>
  </dependency>

  <dependency>
    <groupId>eu.toolchain.serializer</groupId>
    <artifactId>tiny-serializer-processor</artifactId>
    <version>${tiny.version}</version>
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

##### Providers

Providers are supported by annotating a field with `@AutoSerialize.Field(provided = true)`.
This will cause the serializer to be generated with additional constructor parameters, expecting a provided implementation.

Example:

* [Provider Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Provided.java)
  (generates: [Provider_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Provided_Serializer.java))

##### Controlling Accessor

The accessor to use to fetch the value of a field can be controlled with `@AutoSerialize.Field(accessor = <name>)`.

This will cause the generated serializer to use a method called `<name>` when fetching the value for that field.

* [Custom Accessor Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/CustomAccessor.java)
  (generates: [CustomAccessor_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/CustomAccessor_Serializer.java))

##### Ignoring Fields

Fields can be ignored using the `@AutoSerialize.Ignore` annotation.

* [Ignore Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Ignore.java)
  (generates: [Ignore_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Ignore_Serializer.java))

##### Immutable Collections

TinySerializer can be configured to use (guava) immutable collections when
building collection-oriented serializers.
Take note that this does _not_ mean that the serializer guarantees that the
input collection is immutable, only that the collections produced by the
framework are.

You accomplish this by using the `#useImmutableCollections(boolean)`
configuration on the builder, like the following.

```java
TinySerializer.builder().useImmutableCollections(true).build()
```

Due to `tiny-serializer-core` not depending on guava, this is something that
you will have to add to your project in order for this to work, otherwise it
will throw an exception at configuration time.

```xml
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>${guava.version}</version>
</dependency>
```

Examples:

* [Serialize an Immutable Map](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeImmutableMap.java)

#### Immutable Objects

Immutable objects are notoriously _hard_ to automatically serialize.

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

##### Implicit use of Constructor

The default strategy which involves blindly taking all the fields, and mashing
them in the discovered order into the constructor.

This works pretty good because the the annotation processing happens early in
the compiler, causing it to be able to verify that at least a matching
signature _is_ available.

It also meshes very well with lombok's `@RequiredFieldConstructor`.

Earlier version of *Eclipse* have a fundamental issue in that the AST provided
to the processor does not guarantee that fields are exposed in the same order
at which they are defined (see [this bug](https://bugs.eclipse.org/bugs/show_bug.cgi?id=300408)).
This should have been resolved in Eclipse 3.5.

If you are unable to upgrade eclipse you can combat the problem with the
`@AutoSerialize.Field(id = <int>)` annotation, where the default strategy will
be to order the fields according to the value of this field and use the
matching constructor.

Examples:

* [Fields Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Fields.java)
  (generates: [Fields_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Fields_Serializer.java))
* [Ordering Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Ordering.java)
  (generates: [Ordering_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/Ordering_Serializer.java))

##### Builder Types

The serialized object can specify a builder type through
`@AutoSerialize.Builder`.

If specified, all field construction will be delegated to the specified builder.

The following is an example using lombok's [`@Builder`](https://projectlombok.org/features/Builder.html).

```java
@AutoSerialize(builder = @AutoSerialize.Builder())
@Builder
@Data
public class LombokSerializedObject {
    public int version;
    public String field;
    public Map<String, String> map;
    public List<String> someStrings;
}
```

*Note*: This should only be used as an example, lombok's `@Data` annotation
already generates a suitable constructor which provides `@AutoSerialize` with
the means to construct a new instance.

The following is an example using [`@AutoMatter`](https://github.com/danielnorberg/auto-matter).

```java
@AutoSerialize(builder = @AutoSerialize.Builder(type = AutoMatterSerializedObjectBuilder.class))
@AutoMatter
public interface AutoMatterSerializedObject {
    public int version();
    public String field();
    public Map<String, String> map();
    public List<String> someStrings();
}
```

Examples:

* [Use Builder Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilder.java)
  (generates: [UseBuilder_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilder_Serializer.java))
* [Use Builder Constructor Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilderConstructor.java)
  (generates: [UseBuilderConstructor_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/UseBuilderConstructor_Serializer.java))
* [Interface Using Builder Test](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/InterfaceUsingBuilder.java)
  (generates: [UseBuilderConstructor_Serializer](tiny-serializer-processor/src/test/resources/eu/toolchain/serializer/processor/InterfaceUsingBuilder_Serializer.java))

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
* [MessagePack](https://github.com/msgpack/msgpack-java)
