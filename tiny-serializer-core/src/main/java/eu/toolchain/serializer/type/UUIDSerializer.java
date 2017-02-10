package eu.toolchain.serializer.type;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UUIDSerializer implements Serializer<UUID> {
  private final Serializer<Long> longS;

  @Override
  public void serialize(SerialWriter buffer, UUID value) throws IOException {
    longS.serialize(buffer, value.getMostSignificantBits());
    longS.serialize(buffer, value.getLeastSignificantBits());
  }

  @Override
  public UUID deserialize(SerialReader buffer) throws IOException {
    final long most = longS.deserialize(buffer);
    final long least = longS.deserialize(buffer);
    return new UUID(most, least);
  }
}
