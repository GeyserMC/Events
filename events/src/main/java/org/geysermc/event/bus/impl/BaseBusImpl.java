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

import static io.leangen.geantyref.GenericTypeReflector.erase;

import io.leangen.geantyref.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.event.FireResult;
import org.geysermc.event.bus.BaseBus;
import org.geysermc.event.bus.impl.util.Utils;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;
import org.geysermc.event.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BaseBusImpl<E, S extends Subscriber<? extends E>> implements BaseBus<E, S> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Class<?>, Set<Subscriber<?>>> subscribers = Collections.synchronizedMap(new HashMap<>());
    private final Map<Class<?>, List<Subscriber<?>>> sortedSubscribersCache =
            Collections.synchronizedMap(new HashMap<>());

    private final Class<?> baseEventType;

    public BaseBusImpl() {
        baseEventType = erase(new TypeToken<E>() {}.getType());
    }

    protected <T extends E> void register(Class<T> eventClass, S subscriber) {
        if (!baseEventType.isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException(
                    "Event %s has to be assignable from %s".formatted(eventClass, baseEventType));
        }
        if (!subscriber.eventClass().isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException(String.format(
                    "Tried to subscribe to %s with a subscriber that listens to %s, which is not assignable from %s",
                    eventClass, subscriber.eventClass(), eventClass));
        }
        synchronized (subscribers) {
            subscribers.computeIfAbsent(eventClass, $ -> new HashSet<>()).add(subscriber);
            sortedSubscribersCache.remove(eventClass);
        }
    }

    protected <T extends E> void findSubscriptions(
            @NonNull Object listener, TriConsumer<Class<T>, Subscribe, BiConsumer<Object, T>> consumer) {
        try {
            Class<?> listenerClass = listener.getClass();
            Class<?> generated = Class.forName(listenerClass.getPackageName() + ".$" + listenerClass.getSimpleName());
            SubscriptionFinderGenerated.findSubscriptions(baseEventType, generated, consumer);
        } catch (ClassNotFoundException ignored) {
            // use reflection based
            SubscriptionFinderReflectionInvoke.findSubscriptions(baseEventType, listener, consumer);
        }
    }

    @Override
    public void unsubscribe(@NonNull S subscription) {
        synchronized (subscribers) {
            // we can trust the subscription because the implementation that will be used is final.
            Class<? extends E> eventClass = subscription.eventClass();

            subscribers.computeIfPresent(eventClass, ($, value) -> {
                value.remove(subscription);
                if (value.isEmpty()) {
                    return null;
                }
                return value;
            });
            sortedSubscribersCache.remove(eventClass);
        }
    }

    protected void unsubscribeMany(Iterable<S> subscriptions) {
        synchronized (subscribers) {
            for (S subscription : subscriptions) {
                unsubscribe(subscription);
            }
        }
    }

    protected void unsubscribeAll() {
        subscribers.clear();
        sortedSubscribersCache.clear();
    }

    @Override
    public FireResult fire(@NonNull E event) {
        FireResult result = fireSilently(event);
        if (!result.success()) {
            result.exceptions().forEach((subscriber, throwable) -> {
                logger.error(
                        "An exception occurred while executing event {} for subscriber {}",
                        event.getClass().getSimpleName(),
                        subscriber.getClass().getName(),
                        throwable);
            });
        }
        return result;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public FireResult fireSilently(@NonNull E event) {
        Map<Subscriber<?>, Throwable> thrown = new HashMap<>();

        for (Subscriber subscriber : sortedSubscribers(event.getClass())) {
            if (Utils.shouldCallSubscriber(subscriber, event)) {
                try {
                    subscriber.invoke(event);
                } catch (Throwable throwable) {
                    thrown.put(subscriber, throwable);
                }
            }
        }

        return FireResult.resultFor(thrown);
    }

    @SuppressWarnings("unchecked")
    protected List<S> sortedSubscribers(Class<?> eventClass) {
        var sortedSubscribers = (List<S>) sortedSubscribersCache.get(eventClass);
        if (sortedSubscribers != null) {
            return sortedSubscribers;
        }

        Set<Class<?>> ancestors = Utils.ancestorsThatUse(eventClass, baseEventType);

        sortedSubscribers = new ArrayList<>();
        synchronized (subscribers) {
            for (Class<?> ancestor : ancestors) {
                sortedSubscribers.addAll(castGenericNullableSet(subscribers.get(ancestor)));
            }
        }
        sortedSubscribers.sort(Comparator.comparingInt(s -> s.order().ordinal()));
        sortedSubscribers = Collections.unmodifiableList(sortedSubscribers);
        return sortedSubscribers;
    }

    protected <T extends Subscriber<U>, U> Set<T> eventSubscribers(Class<U> eventType) {
        return castGenericNullableSet(subscribers.get(eventType));
    }

    @SuppressWarnings("unchecked")
    protected static <T extends U, U> Set<T> castGenericNullableSet(@Nullable Set<U> o) {
        return o != null ? (Set<T>) o : Collections.emptySet();
    }
}
