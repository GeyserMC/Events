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

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import org.geysermc.event.bus.impl.util.GeneratedSubscriberInfo;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.util.TriConsumer;

final class SubscriptionFinderGenerated {
    @SuppressWarnings({"rawtypes", "unchecked"})
    static <E> void findSubscriptions(
            Class<?> baseEventType,
            Class<?> generated,
            TriConsumer<Class<E>, Subscribe, BiConsumer<Object, E>> consumer) {

        List<GeneratedSubscriberInfo> subscribers;
        try {
            Field events = generated.getDeclaredField("events");
            events.setAccessible(true);
            subscribers = (List<GeneratedSubscriberInfo>) events.get(null);
        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException exception) {
            throw new IllegalStateException(
                    String.format("Generated class %s doesn't follow the expected event structure!", generated),
                    exception);
        }

        for (GeneratedSubscriberInfo info : subscribers) {
            if (!baseEventType.isAssignableFrom(info.eventClass())) {
                continue;
            }
            consumer.accept(info.eventClass(), info.subscribe(), info.consumer());
        }
    }
}
