package eu.toolchain.serializer.processor;

import java.util.function.Consumer;
import javax.annotation.processing.Messager;
import lombok.Getter;

public class BrokenException extends RuntimeException {
  @Getter
  private final Consumer<Messager> writer;

  public BrokenException(final String message, final Consumer<Messager> writer) {
    super(message);
    this.writer = writer;
  }
}
