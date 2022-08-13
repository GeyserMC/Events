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

package org.geysermc.event.bus;

import java.util.Set;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.subscribe.OwnedSubscriber;
import org.geysermc.event.subscribe.Subscriber;

public interface OwnedEventBus<O, E, S extends OwnedSubscriber<O, ? extends E>> extends BaseBus<E, S> {
  /**
   * Subscribes to the given event see {@link Subscriber}.
   * <p>
   * The difference between this method and
   * is that this method takes in an extension parameter which allows for
   * the event to be unsubscribed upon extension disable and reloads.
   *
   * @param owner      the extension to subscribe the event to
   * @param eventClass the class of the event
   * @param consumer   the consumer for handling the event
   * @param <T>        the event class
   * @return the event subscription
   */
  @NonNull <T extends E, U extends OwnedSubscriber<O, T>> U subscribe(
      @NonNull O owner,
      @NonNull Class<T> eventClass,
      @NonNull Consumer<T> consumer
  );

  /**
   * Registers events for the given listener.
   *
   * @param owner    the extension registering the event
   * @param listener the listener
   */
  void register(@NonNull O owner, @NonNull Object listener);

  /**
   * Unregisters all events from a given owner.
   *
   * @param owner the extension
   */
  void unregisterAll(@NonNull O owner);

  @Override
  @NonNull
  <T extends E> Set<? extends OwnedSubscriber<O, T>> subscribers(@NonNull Class<T> eventClass);
}
