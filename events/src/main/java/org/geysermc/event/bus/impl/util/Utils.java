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

import java.util.HashSet;
import java.util.Set;
import org.geysermc.event.Cancellable;
import org.geysermc.event.subscribe.Subscriber;

public final class Utils {
    public static boolean isEventCancelled(Object event) {
        return event instanceof Cancellable && ((Cancellable) event).cancelled();
    }

    public static boolean shouldCallSubscriber(Subscriber<?> subscriber, Object event) {
        return subscriber.ignoreCancelled() || !isEventCancelled(event);
    }

    /**
     * Looks at all the superclasses and superinterfaces of a given class and checks if it is
     * assignable from the given base class. Superclasses and superinterfaces of the base class and
     * the base class itself are not included in the result, but the given class is included if a
     * super type is assignable.
     *
     * @param clazz the class to look at
     * @param use the base class
     */
    public static Set<Class<?>> ancestorsThatUse(Class<?> clazz, Class<?> use) {
        Set<Class<?>> types = new HashSet<>();
        ancestorsThatUse(types, clazz, use);
        return types;
    }

    private static boolean ancestorsThatUse(Set<Class<?>> types, Class<?> type, Class<?> use) {
        // we don't have to continue searching once the base class is found
        if (type == use) {
            return true;
        }

        boolean shouldAdd = false;
        for (Class<?> clazz : type.getInterfaces()) {
            shouldAdd |= ancestorsThatUse(types, clazz, use);
        }

        Class<?> superClass = type.getSuperclass();
        if (superClass != null) {
            shouldAdd |= ancestorsThatUse(types, superClass, use);
        }

        if (shouldAdd) {
            types.add(type);
        }
        return shouldAdd;
    }
}
