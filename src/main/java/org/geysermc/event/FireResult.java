package org.geysermc.event;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.subscribe.Subscriber;

public class FireResult {
  private static final FireResult OK = new FireResult(Collections.emptyMap());

  private final Map<Subscriber<?>, Throwable> exceptions;

  private FireResult(Map<Subscriber<?>, Throwable> exceptions) {
    this.exceptions = Collections.unmodifiableMap(exceptions);
  }

  public static FireResult ok() {
    return OK;
  }

  public boolean success() {
    return exceptions.isEmpty();
  }

  public Map<Subscriber<?>, Throwable> exceptions() {
    return exceptions;
  }

  public static FireResult resultFor(@NonNull Map<Subscriber<?>, Throwable> exceptions) {
    Objects.requireNonNull(exceptions);
    if (exceptions.isEmpty()) {
      return ok();
    }
    return new FireResult(exceptions);
  }
}
