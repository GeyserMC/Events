package org.geysermc.event;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.MinLen;

public class FireResult {
  private static final FireResult OK = new FireResult(Collections.emptyList());

  private final List<Throwable> exceptions;

  private FireResult(List<Throwable> exceptions) {
    this.exceptions = Collections.unmodifiableList(exceptions);
  }

  public static FireResult ok() {
    return OK;
  }

  public boolean success() {
    return exceptions.isEmpty();
  }

  public List<Throwable> exceptions() {
    return exceptions;
  }

  public static FireResult failure(@NonNull @MinLen(1) List<Throwable> exceptions) {
    Objects.requireNonNull(exceptions);
    if (exceptions.size() == 0) {
      throw new IllegalArgumentException("Failure requires at least one exception");
    }
    return new FireResult(exceptions);
  }

  public static FireResult resultFor(@NonNull List<Throwable> exceptions) {
    Objects.requireNonNull(exceptions);
    if (exceptions.isEmpty()) {
      return ok();
    }
    return new FireResult(exceptions);
  }
}
