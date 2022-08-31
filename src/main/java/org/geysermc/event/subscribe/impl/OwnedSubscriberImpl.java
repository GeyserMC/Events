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

package org.geysermc.event.subscribe.impl;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.PostOrder;
import org.geysermc.event.subscribe.OwnedSubscriber;

public abstract class OwnedSubscriberImpl<O, E> extends SubscriberImpl<E>
    implements OwnedSubscriber<O, E> {

  protected final O owner;

  public OwnedSubscriberImpl(
      @NonNull O owner,
      @NonNull Class<E> eventClass,
      @NonNull Consumer<E> handler,
      @NonNull PostOrder postOrder
  ) {
    super(eventClass, handler, postOrder);
    this.owner = owner;
  }

  public <H> OwnedSubscriberImpl(
      @NonNull O owner,
      @NonNull Class<E> eventClass,
      @NonNull PostOrder postOrder,
      boolean ignoreCancelled,
      @NonNull H handlerInstance,
      @NonNull BiConsumer<H, E> handler
  ) {
    super(eventClass, postOrder, ignoreCancelled, handlerInstance, handler);
    this.owner = owner;
  }

  @Override
  public @NonNull O owner() {
    return owner;
  }
}
