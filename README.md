# TinySerializer

A small serialization framework for Java.

* Composable serializers with very little fuzz.
* Primitive serializers are external to the the I/O abstraction, making the
  reading and writing very simple to implement efficiently and correctly.
* This is not a statically initialized framework (unless you make it so),
  you'll have to pass an instance of SerializerFramework around (like you should).

# Setup

Add tiny-serializer-core as a dependency to your project, and tiny-serializer-api as
a dependency to your public API (if you have any).

```
<dependency>
  <groupId>eu.toolchain.serializer</groupId>
  <artifactId>tiny-serializer-core</artifactId>
  <version>CHANGEME</version>
</dependency>
```

After that, the first step is to instantiate the framework.

See [SerializerSetup.java](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializerSetup.java)
for an example of how to do this.

# Usage

The following section contains documentation on how to use TinySerializer.

## Basic serialization

At the core, all serializers implement ```Serializer<T>```, where ```T``` is the type of the value being serialized.

Serialization happens through two methods, ```Serializer#serialize(SerialWriter, T)```, and ```T Serializer#deserialzie(SerialReader)```.

The instance of a ```Serializer``` is completely stateless, and therefore thread safe.

```SerialReader``` and ```SerialWriter``` are the two primary I/O interfaces, they perform reading and writing respectively.

The bundled implementations of these can be found in [```eu.toolchain.serializer.io```](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io), and implementing your own is documented further down in [Implementing a Writer/Reader](#implementing-a-writer-reader).

## Serializing primitive values

The following methods give access to primitive serializers.

* ```Serializer<Short> TinySerializer#shortNumber()```
* ```Serializer<Integer> TinySerializer#integer()```
* ```Serializer<Long> TinySerializer#longNumber()```
* ```Serializer<Boolean> TinySerializer#bool()```
* ```Serializer<Float> TinySerializer#floatNumber()```
* ```Serializer<Double> TinySerializer#doubleNumber()```

```byte``` is omitted since it is part of the I/O abstraction.

```char``` is omitted since I haven't had use for it yet, they typically come
in strings for which there are ```TinySerializer#string()```.

Examples:

* [Primitive example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializePrimitiveExample.java)

## Implementing a Writer/Reader

```SerialReader``` and ```SerialWriter``` are the two primary I/O interfaces, they perform reading and writing respectively.

To implement a custom ```SerialWriter```, use the available [```AbstractSerialWriter```](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io/AbstractSerialWriter.java) as a base.

To implement a custom ```SerialReader```, use the available [```AbstractSerialReader```](tiny-serializer-core/src/main/java/eu/toolchain/serializer/io/AbstractSerialReader.java) as a base.

Examples:

* [Custom reader example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeCustomReaderExample.java)
* [Custom writer example](tiny-serializer-core/src/example/java/eu/toolchain/examples/SerializeCustomWriterExample.java)
