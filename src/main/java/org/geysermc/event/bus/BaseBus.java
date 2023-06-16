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
package org.geysermc.event.bus;

import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.FireResult;
import org.geysermc.event.subscribe.Subscriber;

public interface BaseBus<E, S extends Subscriber<? extends E>> {
    /**
     * Unsubscribes the given {@link Subscriber}.
     *
     * @param subscription the event subscription
     */
    void unsubscribe(@NonNull S subscription);

    /**
     * Fires the given event and log all exceptions that occur while executing this event.
     *
     * @param event the event to fire
     */
    FireResult fire(@NonNull E event);

    /**
     * Fires the given event silently.
     *
     * @param event the event to fire
     * @return the result of firing the given event
     */
    FireResult fireSilently(@NonNull E event);

    /**
     * Gets the subscriptions for the given event class.
     *
     * @param eventClass the event class
     * @param <T>        the value
     * @return the subscriptions for the event class
     */
    @NonNull <T extends E> Set<? extends Subscriber<T>> subscribers(@NonNull Class<T> eventClass);
}
