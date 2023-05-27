package org.geysermc.event.util;

import java.util.List;

public class CombinedException extends RuntimeException {
  private final List<Throwable> exceptions;

  public CombinedException(String message, List<Throwable> exceptions) {
    super(message, null, true, false);
    this.exceptions = exceptions;

    for (Throwable exception : exceptions) {
      addSuppressed(exception);
    }
  }

  public List<Throwable> exceptions() {
    return exceptions;
  }
}
