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
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.bus.BaseBus;
import org.geysermc.event.bus.impl.util.SubscriberUtils;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;
import org.geysermc.event.util.TriConsumer;
import org.lanternpowered.lmbda.LambdaFactory;

abstract class BaseBusImpl<E, S extends Subscriber<? extends E>> implements BaseBus<E, S> {
  private static final MethodHandles.Lookup CALLER = MethodHandles.lookup();

  private final SetMultimap<Class<?>, Subscriber<?>> subscribers =
      Multimaps.synchronizedSetMultimap(HashMultimap.create());

  private final Class<? super E> eventType;

  @SuppressWarnings("unchecked")
  private final LoadingCache<Class<?>, List<Subscriber<?>>> sortedSubscribersCache =
      CacheBuilder.newBuilder()
          .build(CacheLoader.from(eventClass -> {
            synchronized (subscribers) {
              Set<S> classSubscribers = (Set<S>) subscribers.get(eventClass);

              List<S> sortedSubscribers = new ArrayList<>(classSubscribers);
              sortedSubscribers.sort(Comparator.comparingInt(s -> s.order().ordinal()));
              return Collections.unmodifiableList(sortedSubscribers);
            }
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
      if (subscribers.remove(eventClass, subscription)) {
        sortedSubscribersCache.invalidate(eventClass);
      }
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
    synchronized (subscribers) {
      subscribers.clear();
    }
    sortedSubscribersCache.invalidateAll();
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public boolean fire(@NonNull E event) {
    boolean successful = true;

    for (Subscriber subscriber : sortedSubscribers(event.getClass())) {
      if (SubscriberUtils.shouldCall(subscriber, event)) {
        try {
          subscriber.invoke(event);
        } catch (Throwable throwable) {
          successful = false;
        }
      }
    }

    return successful;
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
