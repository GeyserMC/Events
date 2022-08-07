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

package org.geysermc.event.bus.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.bus.OwnedBus;
import org.geysermc.event.subscribe.OwnedSubscriber;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;

public abstract class OwnedBusImpl<O, E, S extends OwnedSubscriber<O, ? extends E>>
    extends BaseBusImpl<E, S>
    implements OwnedBus<O, E, S> {

  private final SetMultimap<O, Subscriber<?>> ownedSubscribers =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  protected abstract <L, T extends E, B extends OwnedSubscriber<O, T>> B makeSubscription(
      O owner,
      Class<T> eventClass,
      Subscribe subscribe,
      L listener,
      BiConsumer<L, T> handler
  );

  protected abstract <T extends E, B extends OwnedSubscriber<O, T>> B makeSubscription(
      O owner,
      Class<T> eventClass,
      Consumer<T> handler
  );

  @Override
  @NonNull
  @SuppressWarnings("unchecked")
  public <T extends E, U extends OwnedSubscriber<O, T>> U subscribe(
      @NonNull O owner,
      @NonNull Class<T> eventClass,
      @NonNull Consumer<T> handler
  ) {
    OwnedSubscriber<O, T> subscription =
        makeSubscription(owner, eventClass, handler);

    synchronized (ownedSubscribers) {
      if (ownedSubscribers.put(owner, subscription)) {
        register(eventClass, (S) subscription);
      }
      return (U) subscription;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void register(@NonNull O owner, @NonNull Object listener) {
    findSubscriptions(listener, (eventClass, subscribe, handler) -> {
      S subscriber =
          (S) makeSubscription(owner, eventClass, subscribe, listener, handler);

      register(eventClass, subscriber);
      ownedSubscribers.put(owner, subscriber);
    });
  }

  @Override
  public void unregisterAll(@NonNull O owner) {
    unsubscribeMany(castGenericSet(ownedSubscribers.removeAll(owner)));
  }

  @Override
  @NonNull
  public <T extends E> Set<? extends OwnedSubscriber<O, T>> subscribers(
      @NonNull Class<T> eventClass
  ) {
    return Collections.unmodifiableSet(eventSubscribers(eventClass));
  }
}
