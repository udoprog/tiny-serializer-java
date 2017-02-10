package eu.toolchain.serializer.processor;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ShortIterator implements Iterator<Short> {
  private int index = 0;

  @Override
  public boolean hasNext() {
    return index + 1 <= Short.MAX_VALUE;
  }

  @Override
  public Short next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more positive short values available");
    }

    return (short) index++;
  }
}
