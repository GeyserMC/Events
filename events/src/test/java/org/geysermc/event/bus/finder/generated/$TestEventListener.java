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
package org.geysermc.event.bus.finder.generated;

import java.util.ArrayList;
import java.util.List;
import org.geysermc.event.PostOrder;
import org.geysermc.event.TestChildEvent;
import org.geysermc.event.TestEvent;
import org.geysermc.event.bus.impl.util.GeneratedSubscriberInfo;

/**
 * Automatically generated event method references
 */
final class $TestEventListener {
    private static final List<GeneratedSubscriberInfo<?, ?>> events;

    static {
        events = new ArrayList<>();
        events.add(new GeneratedSubscriberInfo<>(
                TestEvent.class, PostOrder.NORMAL, false, TestEventListener::publicEvent));
        events.add(new GeneratedSubscriberInfo<>(
                TestEvent.class, PostOrder.NORMAL, false, TestEventListener::defaultVisibilityEvent));
        events.add(new GeneratedSubscriberInfo<>(Object.class, PostOrder.NORMAL, false, TestEventListener::baseEvent));
        events.add(new GeneratedSubscriberInfo<>(
                TestChildEvent.class, PostOrder.NORMAL, false, TestEventListener::childEvent));
    }
}
