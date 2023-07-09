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
package org.geysermc.event.bus.impl.util;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.PostOrder;
import org.geysermc.event.subscribe.Subscribe;

public final class GeneratedSubscriberInfo<T, E> {
    private final Class<E> eventClass;
    private final Subscribe subscribe;
    private final BiConsumer<T, E> consumer;

    public GeneratedSubscriberInfo(
            Class<E> eventClass, PostOrder order, boolean ignoreCancelled, BiConsumer<T, E> consumer) {
        this.eventClass = eventClass;
        this.subscribe = new SubscribeImpl(order, ignoreCancelled);
        this.consumer = consumer;
    }

    public Class<E> eventClass() {
        return eventClass;
    }

    public Subscribe subscribe() {
        return subscribe;
    }

    public BiConsumer<T, E> consumer() {
        return consumer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GeneratedSubscriberInfo<?, ?>) obj;
        return Objects.equals(this.eventClass, that.eventClass)
                && Objects.equals(this.subscribe, that.subscribe)
                && Objects.equals(this.consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventClass, subscribe, consumer);
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private record SubscribeImpl(PostOrder order, boolean ignoreCancelled) implements Subscribe {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Subscribe.class;
        }

        @Override
        public @NonNull PostOrder postOrder() {
            return order;
        }
    }
}
