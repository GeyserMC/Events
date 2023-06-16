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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.PostOrder;
import org.geysermc.event.bus.EventBus;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;

public abstract class EventBusImpl<E, S extends Subscriber<? extends E>> extends BaseBusImpl<E, S>
        implements EventBus<E, S> {

    protected abstract <H, T extends E, B extends Subscriber<T>> B makeSubscription(
            @NonNull Class<T> eventClass,
            @NonNull Subscribe subscribe,
            @NonNull H listener,
            @NonNull BiConsumer<H, T> handler);

    protected abstract <T extends E, B extends Subscriber<T>> B makeSubscription(
            @NonNull Class<T> eventClass, @NonNull Consumer<T> handler, @NonNull PostOrder postOrder);

    @Override
    @SuppressWarnings("unchecked")
    public void register(@NonNull Object listener) {
        findSubscriptions(listener, (eventType, subscribe, handler) -> {
            S subscriber = (S) makeSubscription(eventType, subscribe, listener, handler);

            register(eventType, subscriber);
        });
    }

    @Override
    @NonNull public <T extends E, U extends Subscriber<T>> U subscribe(
            @NonNull Class<T> eventClass, @NonNull Consumer<T> consumer) {
        return subscribe(eventClass, consumer, PostOrder.NORMAL);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <T extends E, U extends Subscriber<T>> U subscribe(
            @NonNull Class<T> eventClass, @NonNull Consumer<T> consumer, @NonNull PostOrder postOrder) {
        U subscription = makeSubscription(eventClass, consumer, postOrder);
        register(eventClass, (S) subscription);
        return subscription;
    }

    @Override
    public void unregisterAll() {
        super.unsubscribeAll();
    }

    @Override
    @NonNull public <T extends E> Set<? extends Subscriber<T>> subscribers(@NonNull Class<T> eventClass) {
        return Collections.unmodifiableSet(eventSubscribers(eventClass));
    }
}
