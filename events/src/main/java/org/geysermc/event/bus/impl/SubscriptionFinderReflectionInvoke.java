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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.util.TriConsumer;

final class SubscriptionFinderReflectionInvoke {
    @SuppressWarnings("unchecked")
    static <E> void findSubscriptions(
            Class<?> baseEventType, Object listener, TriConsumer<Class<E>, Subscribe, BiConsumer<Object, E>> consumer) {

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

                if (!baseEventType.isAssignableFrom(firstParameterType)) {
                    continue;
                }

                // allow private subscribers
                method.setAccessible(true);

                consumer.accept((Class<E>) firstParameterType, subscribe, (instance, eventInstance) -> {
                    try {
                        method.invoke(instance, eventInstance);
                    } catch (IllegalAccessException | InvocationTargetException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
            currentClass = currentClass.getSuperclass();
        }
    }
}
