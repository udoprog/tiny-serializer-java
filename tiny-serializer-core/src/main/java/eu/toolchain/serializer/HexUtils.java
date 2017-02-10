package eu.toolchain.serializer;

public class HexUtils {
  // @formatter:off
    private static final char[] hex = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    // @formatter:on

  public static String toHex(byte[] value) {
    final StringBuilder b = new StringBuilder();

    for (byte c : value) {
      b.append(hex[(c >>> 4) & 0xf]).append(hex[c & 0xf]);
    }

    return "0x" + b.toString();
  }
}
