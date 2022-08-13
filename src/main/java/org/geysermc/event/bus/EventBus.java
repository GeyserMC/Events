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

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.subscribe.Subscriber;

public interface EventBus<E, S extends Subscriber<? extends E>> extends BaseBus<E, S> {
  /**
   * Registers events for the given listener.
   *
   * @param listener the listener
   */
  void register(@NonNull Object listener);

  @NonNull <T extends E, U extends Subscriber<T>> U subscribe(
      @NonNull Class<T> eventClass,
      @NonNull Consumer<T> consumer
  );

  void unregisterAll();
}