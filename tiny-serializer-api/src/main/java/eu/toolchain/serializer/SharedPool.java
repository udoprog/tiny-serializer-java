package eu.toolchain.serializer;

import java.nio.ByteBuffer;

/**
 * Memory pooling is used to reduce the number of allocations required during a serialization
 * round.
 * <p>
 * Implementations can assume that they are run in a single-threaded context.
 *
 * @author udoprog
 */
public interface SharedPool {
  /**
   * Allocate a buffer of the given size.
   * <p>
   * This will allocate a shared buffer that is associated with the given writer.
   *
   * @param size The maximum size of the buffer, it will be sized accordingly.
   * @return A shared byte buffer.
   */
  public ByteBuffer allocate(int size);

  /**
   * Release a previously allocated buffer.
   *
   * @param size The size to release.
   */
  public void release(int size);
}
