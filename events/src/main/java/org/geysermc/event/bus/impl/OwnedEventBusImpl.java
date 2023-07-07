/*
 * Copyright (c) 2022-2023 GeyserMC <https://geysermc.org>
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.PostOrder;
import org.geysermc.event.bus.OwnedEventBus;
import org.geysermc.event.subscribe.OwnedSubscriber;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;

public abstract class OwnedEventBusImpl<O, E, S extends OwnedSubscriber<O, ? extends E>> extends BaseBusImpl<E, S>
        implements OwnedEventBus<O, E, S> {

    private final Map<O, Set<Subscriber<?>>> ownedSubscribers = Collections.synchronizedMap(new HashMap<>());

    protected abstract <L, T extends E, B extends OwnedSubscriber<O, T>> B makeSubscription(
            @NonNull O owner,
            @NonNull Class<T> eventClass,
            @NonNull Subscribe subscribe,
            @NonNull L listener,
            @NonNull BiConsumer<L, T> handler);

    protected abstract <T extends E, B extends OwnedSubscriber<O, T>> B makeSubscription(
            @NonNull O owner, @NonNull Class<T> eventClass, @NonNull Consumer<T> handler, @NonNull PostOrder postOrder);

    @Override
    public <T extends E, U extends OwnedSubscriber<O, T>> @NonNull U subscribe(
            @NonNull O owner, @NonNull Class<T> eventClass, @NonNull Consumer<T> handler) {
        return subscribe(owner, eventClass, handler, PostOrder.NORMAL);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E, U extends OwnedSubscriber<O, T>> @NonNull U subscribe(
            @NonNull O owner,
            @NonNull Class<T> eventClass,
            @NonNull Consumer<T> handler,
            @NonNull PostOrder postOrder) {
        OwnedSubscriber<O, T> subscription = makeSubscription(owner, eventClass, handler, postOrder);

        synchronized (ownedSubscribers) {
            if (ownedSubscribers.computeIfAbsent(owner, $ -> new HashSet<>()).add(subscription)) {
                register(eventClass, (S) subscription);
            }
            return (U) subscription;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(@NonNull O owner, @NonNull Object listener) {
        findSubscriptions(listener, (eventClass, subscribe, handler) -> {
            S subscriber = (S) makeSubscription(owner, eventClass, subscribe, listener, handler);

            register(eventClass, subscriber);
            synchronized (ownedSubscribers) {
                ownedSubscribers.computeIfAbsent(owner, $ -> new HashSet<>()).add(subscriber);
            }
        });
    }

    @Override
    public void unregisterAll(@NonNull O owner) {
        synchronized (ownedSubscribers) {
            unsubscribeMany(castGenericNullableSet(ownedSubscribers.remove(owner)));
        }
    }

    @Override
    public <T extends E> @NonNull Set<? extends OwnedSubscriber<O, T>> subscribers(@NonNull Class<T> eventClass) {
        return Collections.unmodifiableSet(eventSubscribers(eventClass));
    }
}
