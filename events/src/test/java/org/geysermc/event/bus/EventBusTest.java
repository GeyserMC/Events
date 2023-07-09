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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;
import org.geysermc.event.Cancellable;
import org.geysermc.event.Counter;
import org.geysermc.event.Event;
import org.geysermc.event.FireResult;
import org.geysermc.event.PostOrder;
import org.geysermc.event.TestChildEvent;
import org.geysermc.event.TestEvent;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.TestSubscriberImpl;
import org.geysermc.event.util.AbstractCancellable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventBusTest {
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
    void subscribeToEvent() {
        assertTrue(bus.subscribers(TestEvent.class).isEmpty());
        bus.subscribe(TestEvent.class, event -> {});
        assertEquals(1, Counter.byId("createdConsumerSubscriptions"));
        assertEquals(1, bus.subscribers(TestEvent.class).size());
    }

    @Test
    void subscribeOneUnregisterOne() {
        TestSubscriberImpl<TestEvent> subscription = bus.subscribe(TestEvent.class, event -> {});
        assertEquals(1, bus.subscribers(TestEvent.class).size());
        bus.unsubscribe(subscription);
        assertTrue(bus.subscribers(TestEvent.class).isEmpty());
    }

    @Test
    void subscribeTwoUnregisterOne() {
        TestSubscriberImpl<TestEvent> subscription = bus.subscribe(TestEvent.class, event -> {});
        bus.subscribe(TestEvent.class, event -> {});
        assertEquals(2, bus.subscribers(TestEvent.class).size());
        bus.unsubscribe(subscription);
        assertEquals(1, bus.subscribers(TestEvent.class).size());
    }

    @Test
    void subscribeOneUnregisterOne2() {
        var handler = new CountConsumer<TestEvent>();

        TestSubscriberImpl<TestEvent> subscription = bus.subscribe(TestEvent.class, handler);
        assertEquals(1, bus.subscribers(TestEvent.class).size());

        // this causes sortedSubscribersCache to be initialized for the given event, which has to be invalidated after
        bus.fireSilently(new TestEvent());
        assertEquals(1, handler.invokeCalls);

        bus.unsubscribe(subscription);

        bus.fireSilently(new TestEvent());
        assertEquals(1, handler.invokeCalls);
        assertTrue(bus.subscribers(TestEvent.class).isEmpty());
    }

    @Test
    void callNormalEvent() {
        CountConsumer<TestEvent> handler = new CountConsumer<>();

        bus.subscribe(TestEvent.class, handler);
        assertEquals(0, Counter.byId("createdInstances"));
        assertEquals(0, handler.invokeCalls);

        TestEvent event = new TestEvent();
        assertEquals(1, Counter.byId("createdInstances"));

        assertDoesNotThrow(() -> bus.fire(event));
        assertEquals(1, Counter.byId("createdInstances"));
        assertEquals(1, handler.invokeCalls);
    }

    @Test
    void callThrowEvent() {
        bus.subscribe(TestEvent.class, event -> {
            throw new RuntimeException();
        });

        // todo check whether error was shown in console
        assertDoesNotThrow(() -> bus.fire(new TestEvent()));
    }

    @Test
    void callNormalEventSilently() {
        CountConsumer<TestEvent> handler = new CountConsumer<>();

        bus.subscribe(TestEvent.class, handler);
        assertEquals(0, Counter.byId("createdInstances"));
        assertEquals(0, handler.invokeCalls);

        TestEvent event = new TestEvent();
        assertEquals(1, Counter.byId("createdInstances"));

        FireResult result = bus.fireSilently(event);
        assertTrue(result.success());
        assertTrue(result.exceptions().isEmpty());

        assertEquals(1, Counter.byId("createdInstances"));
        assertEquals(1, handler.invokeCalls);
    }

    @Test
    void callThrowEventSilently() {
        bus.subscribe(TestEvent.class, event -> {
            throw new RuntimeException();
        });

        FireResult result = bus.fireSilently(new TestEvent());
        assertFalse(result.success());
        assertEquals(1, result.exceptions().size());
    }

    @Test
    void unregisterAll() {
        var handler = new CountConsumer<TestEvent>();
        var childHandler = new CountConsumer<TestChildEvent>();

        assertTrue(bus.subscribers(TestEvent.class).isEmpty());
        assertTrue(bus.subscribers(TestChildEvent.class).isEmpty());
        bus.subscribe(TestEvent.class, handler);
        bus.subscribe(TestChildEvent.class, childHandler);

        assertEquals(1, bus.subscribers(TestEvent.class).size());
        assertEquals(1, bus.subscribers(TestChildEvent.class).size());

        bus.fireSilently(new TestChildEvent());
        assertEquals(1, handler.invokeCalls);
        assertEquals(1, childHandler.invokeCalls);

        bus.unregisterAll();
        assertTrue(bus.subscribers(TestEvent.class).isEmpty());
        assertTrue(bus.subscribers(TestChildEvent.class).isEmpty());

        bus.fireSilently(new TestChildEvent());
        assertEquals(1, handler.invokeCalls);
        assertEquals(1, childHandler.invokeCalls);
    }

    @Test
    void registeredSubscribersCallOrder() {
        TestEventListenerOrder listener = new TestEventListenerOrder();

        bus.register(listener);
        assertEquals(5, Counter.byId("createdMethodSubscriptions"));

        bus.fire(new TestEvent());
        assertEquals(PostOrder.LAST, listener.lastCalled);
    }

    @Test
    void registeredSubscribersIgnoreCancelled() {
        TestCancelledEventListener listener = new TestCancelledEventListener();

        bus.register(listener);
        assertEquals(4, Counter.byId("createdMethodSubscriptions"));

        Cancellable event = new TestCancellableEvent();
        bus.fire(event);
        assertTrue(event.isCancelled());
        assertEquals(3, Counter.byId("callCount"));
    }

    static final class CountConsumer<E extends Event> implements Consumer<E> {
        int invokeCalls = 0;

        @Override
        public void accept(E event) {
            invokeCalls++;
        }
    }

    static final class TestEventListenerOrder {
        private PostOrder lastCalled = null;

        @Subscribe(postOrder = PostOrder.FIRST)
        void shouldCallFirst(TestEvent event) {
            assertNull(lastCalled);
            lastCalled = PostOrder.FIRST;
        }

        @Subscribe(postOrder = PostOrder.EARLY)
        void shouldCallEarly(TestEvent event) {
            assertEquals(PostOrder.FIRST, lastCalled);
            lastCalled = PostOrder.EARLY;
        }

        @Subscribe
        void shouldCallNormal(TestEvent event) {
            assertEquals(PostOrder.EARLY, lastCalled);
            lastCalled = PostOrder.NORMAL;
        }

        @Subscribe(postOrder = PostOrder.LATE)
        void shouldCallLate(TestEvent event) {
            assertEquals(PostOrder.NORMAL, lastCalled);
            lastCalled = PostOrder.LATE;
        }

        @Subscribe(postOrder = PostOrder.LAST)
        void shouldCallLast(TestEvent event) {
            assertEquals(PostOrder.LATE, lastCalled);
            lastCalled = PostOrder.LAST;
        }
    }

    static final class TestCancellableEvent extends AbstractCancellable {}

    static final class TestCancelledEventListener {
        @Subscribe(postOrder = PostOrder.FIRST)
        void firstCancelEvent(TestCancellableEvent event) {
            Counter.increment("callCount");
            assertFalse(event.isCancelled());
            event.setCancelled(true);
        }

        @Subscribe(postOrder = PostOrder.EARLY)
        void earlyShouldIgnore(TestCancellableEvent event) {
            fail("Event should be cancelled");
        }

        @Subscribe(ignoreCancelled = true)
        void normalDeCancelEvent(TestCancellableEvent event) {
            Counter.increment("callCount");
            assertTrue(event.isCancelled());
            event.setCancelled(false);
        }

        @Subscribe(postOrder = PostOrder.LATE)
        void lateReCancelEvent(TestCancellableEvent event) {
            Counter.increment("callCount");
            assertFalse(event.isCancelled());
            event.setCancelled(true);
        }
    }
}
