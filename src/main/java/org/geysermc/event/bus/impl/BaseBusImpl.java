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

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.FireResult;
import org.geysermc.event.bus.BaseBus;
import org.geysermc.event.bus.impl.util.Utils;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;
import org.geysermc.event.util.TriConsumer;
import org.lanternpowered.lmbda.LambdaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
abstract class BaseBusImpl<E, S extends Subscriber<? extends E>> implements BaseBus<E, S> {
  private static final MethodHandles.Lookup CALLER = MethodHandles.lookup();

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final SetMultimap<Class<?>, Subscriber<?>> subscribers =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  private Class<? super E> eventType;

  // adding and removing is all managed in sortedSubscribersCache
  private final SetMultimap<Subscriber<?>, Class<?>> subscriberCacheEntries =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  @SuppressWarnings("unchecked")
  private final LoadingCache<Class<?>, List<Subscriber<?>>> sortedSubscribersCache =
      CacheBuilder.newBuilder()
          .removalListener((listener) -> {
            Class<?> eventClass = (Class<?>) listener.getKey();
            List<Subscriber<?>> subscribers = (List<Subscriber<?>>) listener.getValue();
            synchronized (subscriberCacheEntries) {
              for (Subscriber<?> subscriber : subscribers) {
                subscriberCacheEntries.remove(subscriber, eventClass);
              }
            }
          })
          .build(CacheLoader.from(eventClass -> {
            Set<Class<?>> ancestors = Utils.ancestorsThatUse(eventClass, eventType);

            List<Subscriber<?>> sortedSubscribers = new ArrayList<>();
            synchronized (subscribers) {
              for (Class<?> ancestor : ancestors) {
                sortedSubscribers.addAll(subscribers.get(ancestor));
              }
            }
            sortedSubscribers.sort(Comparator.comparingInt(s -> s.order().ordinal()));
            sortedSubscribers = Collections.unmodifiableList(sortedSubscribers);

            synchronized (subscriberCacheEntries) {
              for (Subscriber<?> subscriber : sortedSubscribers) {
                subscriberCacheEntries.put(subscriber, eventClass);
              }
            }
            return sortedSubscribers;
          }));


  @SuppressWarnings("UnstableApiUsage")
  public BaseBusImpl() {
    eventType = new TypeToken<E>(getClass()) {}.getRawType();
  }

  protected <T extends E> void register(Class<T> eventClass, S subscriber) {
    Preconditions.checkArgument(eventType.isAssignableFrom(eventClass));
    Preconditions.checkArgument(subscriber.eventClass().isAssignableFrom(eventClass));
    synchronized (subscribers) {
      subscribers.put(eventClass, subscriber);
      sortedSubscribersCache.invalidate(eventClass);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends E> void findSubscriptions(
      @NonNull Object listener,
      TriConsumer<Class<T>, Subscribe, BiConsumer<Object, T>> consumer
  ) {
    Class<?> currentClass = listener.getClass();
    while (currentClass != Object.class) {
      for (Method method : currentClass.getDeclaredMethods()) {
        Subscribe subscribe = method.getAnnotation(Subscribe.class);

        if (subscribe == null) {
          continue;
        }

        if (method.getParameterCount() > 1) {
          continue;
        }

        Class<?> firstParameterType = method.getParameters()[0].getType();

        if (!eventType.isAssignableFrom(firstParameterType)) {
          continue;
        }

        // allow private subscribers
        method.setAccessible(true);

        try {
          consumer.accept(
              (Class<T>) firstParameterType,
              subscribe,
              LambdaFactory.createBiConsumer(CALLER.unreflect(method))
          );
        } catch (IllegalAccessException exception) {
          exception.printStackTrace();
        }
      }
      currentClass = currentClass.getSuperclass();
    }
  }

  @Override
  public void unsubscribe(@NonNull S subscription) {
    synchronized (subscribers) {
      // we can trust the subscription because the implementation that will be used is final.
      Class<? extends E> eventClass = subscription.eventClass();
      if (!subscribers.remove(eventClass, subscription)) {
        sortedSubscribersCache.invalidate(eventClass);
      }
    }
  }

  protected void unsubscribeMany(Iterable<S> subscriptions) {
    for (S subscription : subscriptions) {
      unsubscribe(subscription);
    }
  }

  protected void unsubscribeAll() {
    synchronized (subscribers) {
      subscribers.clear();
    }
    sortedSubscribersCache.invalidateAll();
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
            throwable
        );
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
    return (List<S>) sortedSubscribersCache.getUnchecked(eventClass);
  }

  protected <T extends Subscriber<U>, U> Set<T> eventSubscribers(Class<U> eventType) {
    return castGenericSet(subscribers.get(eventType));
  }

  @SuppressWarnings("unchecked")
  protected static <T extends U, U> Set<T> castGenericSet(Set<U> o) {
    return (Set<T>) o;
  }
}
