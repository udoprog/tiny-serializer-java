package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;
import java.util.UUID;

@AutoSerialize(useGetter = false)
public class Fields {
  public static final String DEFAULT_OPTIONAL = "optional";

  private final String string;
  private final Interface requiredCustom;
  private final boolean bool;
  private final byte byteNumber;
  private final short shortNumber;
  private final int integer;
  private final long longNumber;
  private final float floatNumber;
  private final double doubleNumber;
  private final char character;
  private final UUID uuid;
  private final byte[] byteArray;
  private final char[] charArray;
  private final Values enumValue;

  public Fields(
    String string, Interface requiredCustom, boolean bool, byte byteNumber, short shortNumber,
    int integer, long longNumber, float floatNumber, double doubleNumber, char character, UUID uuid,
    byte[] byteArray, char[] charArray, Values enumValue
  ) {
    this.string = string;
    this.requiredCustom = requiredCustom;
    this.bool = bool;
    this.byteNumber = byteNumber;
    this.shortNumber = shortNumber;
    this.integer = integer;
    this.longNumber = longNumber;
    this.floatNumber = floatNumber;
    this.doubleNumber = doubleNumber;
    this.character = character;
    this.uuid = uuid;
    this.byteArray = byteArray;
    this.charArray = charArray;
    this.enumValue = enumValue;
  }

  public String string() {
    return string;
  }

  public Interface requiredCustom() {
    return requiredCustom;
  }

  public boolean bool() {
    return bool;
  }

  public byte byteNumber() {
    return byteNumber;
  }

  public short shortNumber() {
    return shortNumber;
  }

  public int integer() {
    return integer;
  }

  public long longNumber() {
    return longNumber;
  }

  public float floatNumber() {
    return floatNumber;
  }

  public char character() {
    return character;
  }

  public double doubleNumber() {
    return doubleNumber;
  }

  public UUID uuid() {
    return uuid;
  }

  public byte[] byteArray() {
    return byteArray;
  }

  public char[] charArray() {
    return charArray;
  }

  public Values enumValue() {
    return enumValue;
  }

  static enum Values {
    value1, value2
  }
}
