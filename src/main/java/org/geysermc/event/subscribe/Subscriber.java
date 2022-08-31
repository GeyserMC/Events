/*
 * Copyright (c) 2022 GeyserMC <https://geysermc.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Events
 */

package org.geysermc.event.subscribe;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.PostOrder;

/**
 * Represents a subscribed listener to an event. Wraps around
 * the event and is capable of unsubscribing from the event or give
 * information about it.
 *
 * @param <T> the class of the event
 */
public interface Subscriber<T> {
  /**
   * Returns the event class.
   */
  @NonNull Class<T> eventClass();

  /**
   * Returns the post order of this event subscription.
   */
  @NonNull PostOrder order();

  /**
   * Returns if this subscription ignores cancelled events.
   */
  boolean ignoreCancelled();

  /**
   * Invokes a given event.
   *
   * @param event the event to invoke
   */
  void invoke(@NonNull T event) throws Throwable;
}
