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
package org.geysermc.event.bus.finder.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.geysermc.event.Counter;
import org.geysermc.event.TestChildEvent;
import org.geysermc.event.TestEvent;
import org.geysermc.event.bus.TestBusImpl;
import org.geysermc.event.subscribe.Subscribe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubscriptionFinderReflectionInvokeTest {
    private TestBusImpl bus;

    @BeforeEach
    void setupBus() {
        bus = new TestBusImpl();
    }

    @AfterEach
    void resetStuff() {
        Counter.reset();
    }

    @Test
    void registeredSubscribersCallCount() {
        TestEventListener listener = new TestEventListener();

        bus.register(listener);
        assertEquals(5, Counter.byId("createdMethodSubscriptions"));

        bus.fire(new TestEvent());
        assertEquals(1, Counter.byId("publicEvent"));
        assertEquals(1, Counter.byId("privateEvent"));
        assertEquals(1, Counter.byId("defaultVisibilityEvent"));
        assertEquals(0, Counter.byId("baseEvent"));
        assertEquals(0, Counter.byId("childEvent"));
        Counter.reset();

        bus.fire(new TestChildEvent());
        assertEquals(1, Counter.byId("publicEvent"));
        assertEquals(1, Counter.byId("privateEvent"));
        assertEquals(1, Counter.byId("defaultVisibilityEvent"));
        assertEquals(0, Counter.byId("baseEvent"));
        assertEquals(1, Counter.byId("childEvent"));
        Counter.reset();

        bus.fire(new Object());
        assertEquals(0, Counter.byId("publicEvent"));
        assertEquals(0, Counter.byId("privateEvent"));
        assertEquals(0, Counter.byId("defaultVisibilityEvent"));
        assertEquals(0, Counter.byId("baseEvent"));
        assertEquals(0, Counter.byId("childEvent"));
    }

    @Test
    void findSubscribersInListener() {
        TestEventListener listener = new TestEventListener();

        bus.findSubscriptions(listener, (eventClass, subscribe, consumer) -> Counter.increment("methodsFound"));

        assertEquals(5, Counter.byId("methodsFound"));
    }

    static final class TestEventListener {
        @Subscribe
        public void publicEvent(TestEvent event) {
            Counter.increment("publicEvent");
        }

        @Subscribe
        private void privateEvent(TestEvent event) {
            Counter.increment("privateEvent");
        }

        @Subscribe
        void defaultVisibilityEvent(TestEvent event) {
            Counter.increment("defaultVisibilityEvent");
        }

        @Subscribe
        void baseEvent(Object event) {
            Counter.increment("baseEvent");
        }

        @Subscribe
        void childEvent(TestChildEvent event) {
            Counter.increment("childEvent");
        }
    }
}
