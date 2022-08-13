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

package org.geysermc.event.bus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.Cancellable;
import org.geysermc.event.Event;
import org.geysermc.event.PostOrder;
import org.geysermc.event.bus.impl.EventBusImpl;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.event.subscribe.Subscriber;
import org.geysermc.event.subscribe.impl.SubscriberImpl;
import org.geysermc.event.util.AbstractCancellable;
import org.geysermc.event.util.TriConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventBusTest {
  private TestBusImpl bus;

  @BeforeEach
  public void setupBus() {
    bus = new TestBusImpl();
  }

  @AfterEach
  public void resetStuff() {
    TestEvent.createdInstances = 0;
  }

  @Test
  public void subscribeToEvent() {
    assertTrue(bus.subscribers(TestEvent.class).isEmpty());
    bus.subscribe(TestEvent.class, event -> {
    });
    assertEquals(1, bus.createdConsumerSubscriptions);
    assertEquals(1, bus.subscribers(TestEvent.class).size());
  }

  @Test
  public void unsubscribeToEvent() {
    TestSubscriberImpl<TestEvent> subscription = bus.subscribe(TestEvent.class, event -> {
    });
    assertEquals(1, bus.subscribers(TestEvent.class).size());
    bus.unsubscribe(subscription);
    assertTrue(bus.subscribers(TestEvent.class).isEmpty());
  }

  @Test
  public void callNormalEvent() {
    CountConsumer<TestEvent> handler = new CountConsumer<>();

    bus.subscribe(TestEvent.class, handler);
    assertEquals(0, TestEvent.createdInstances);
    assertEquals(0, handler.invokeCalls);

    TestEvent event = new TestEvent();
    assertEquals(1, TestEvent.createdInstances);

    assertTrue(bus.fire(event));
    assertEquals(1, TestEvent.createdInstances);
    assertEquals(1, handler.invokeCalls);
  }

  @Test
  public void callThrowEvent() {
    bus.subscribe(TestEvent.class, event -> {
      throw new RuntimeException();
    });

    assertFalse(bus.fire(new TestEvent()));
  }

  @Test
  public void findSubscribersInListener() {
    TestEventListener listener = new TestEventListener();

    AtomicInteger methodsFound = new AtomicInteger();

    bus.findSubscriptions(
        listener,
        (eventClass, subscribe, consumer) ->
            methodsFound.incrementAndGet()
    );

    assertEquals(4, methodsFound.get());
  }

  @Test
  public void registeredSubscribersCallCount() {
    TestEventListener listener = new TestEventListener();

    bus.register(listener);
    assertEquals(4, bus.createdMethodSubscriptions);

    bus.fire(new TestEvent());
    assertEquals(1, listener.publicEventInvokeCount);
    assertEquals(1, listener.privateEventInvokeCount);
    assertEquals(0, listener.baseEventInvokeCount);
    assertEquals(0, listener.childEventInvokeCount);
    listener.resetCounts();

    bus.fire(new TestChildEvent());
    assertEquals(1, listener.publicEventInvokeCount);
    assertEquals(1, listener.privateEventInvokeCount);
    assertEquals(0, listener.baseEventInvokeCount);
    assertEquals(1, listener.childEventInvokeCount);
    listener.resetCounts();

    bus.fire(new Object());
    assertEquals(0, listener.publicEventInvokeCount);
    assertEquals(0, listener.privateEventInvokeCount);
    assertEquals(0, listener.baseEventInvokeCount);
    assertEquals(0, listener.childEventInvokeCount);
  }

  @Test
  public void registeredSubscribersCallOrder() {
    TestEventListenerOrder listener = new TestEventListenerOrder();

    bus.register(listener);
    assertEquals(5, bus.createdMethodSubscriptions);

    bus.fire(new TestEvent());
    assertEquals(PostOrder.LAST, listener.lastCalled);
  }

  @Test
  public void registeredSubscribersIgnoreCancelled() {
    TestCancelledEventListener listener = new TestCancelledEventListener();

    bus.register(listener);
    assertEquals(4, bus.createdMethodSubscriptions);

    Cancellable event = new TestCancellableEvent();
    bus.fire(event);
    assertTrue(event.isCancelled());
    assertEquals(3, listener.callCount);
  }

  static class TestEvent implements Event {
    static int createdInstances = 0;

    TestEvent() {
      createdInstances++;
    }
  }

  static final class TestChildEvent extends TestEvent {
  }

  static final class CountConsumer<E extends Event> implements Consumer<E> {
    int invokeCalls = 0;

    @Override
    public void accept(E event) {
      invokeCalls++;
    }
  }

  static final class TestEventListener {
    int publicEventInvokeCount = 0;
    int privateEventInvokeCount = 0;
    int baseEventInvokeCount = 0;
    int childEventInvokeCount = 0;

    @Subscribe
    public void publicEvent(TestEvent event) {
      publicEventInvokeCount++;
    }

    @Subscribe
    private void privateEvent(TestEvent event) {
      privateEventInvokeCount++;
    }

    @Subscribe
    public void baseEvent(Object event) {
      baseEventInvokeCount++;
    }

    @Subscribe
    public void childEvent(TestChildEvent event) {
      childEventInvokeCount++;
    }

    public void resetCounts() {
      publicEventInvokeCount = 0;
      privateEventInvokeCount = 0;
      baseEventInvokeCount = 0;
      childEventInvokeCount = 0;
    }
  }

  static final class TestEventListenerOrder {
    private PostOrder lastCalled = null;

    @Subscribe(postOrder = PostOrder.FIRST)
    public void shouldCallFirst(TestEvent event) {
      assertNull(lastCalled);
      lastCalled = PostOrder.FIRST;
    }

    @Subscribe(postOrder = PostOrder.EARLY)
    public void shouldCallEarly(TestEvent event) {
      assertEquals(PostOrder.FIRST, lastCalled);
      lastCalled = PostOrder.EARLY;
    }

    @Subscribe
    public void shouldCallNormal(TestEvent event) {
      assertEquals(PostOrder.EARLY, lastCalled);
      lastCalled = PostOrder.NORMAL;
    }

    @Subscribe(postOrder = PostOrder.LATE)
    public void shouldCallLate(TestEvent event) {
      assertEquals(PostOrder.NORMAL, lastCalled);
      lastCalled = PostOrder.LATE;
    }

    @Subscribe(postOrder = PostOrder.LAST)
    public void shouldCallLast(TestEvent event) {
      assertEquals(PostOrder.LATE, lastCalled);
      lastCalled = PostOrder.LAST;
    }
  }

  static final class TestCancellableEvent extends AbstractCancellable {
  }

  static final class TestCancelledEventListener {
    int callCount;

    @Subscribe(postOrder = PostOrder.FIRST)
    public void firstCancelEvent(TestCancellableEvent event) {
      callCount++;
      assertFalse(event.isCancelled());
      event.setCancelled(true);
    }

    @Subscribe(postOrder = PostOrder.EARLY)
    public void earlyShouldIgnore(TestCancellableEvent event) {
      fail("Event should be cancelled");
    }

    @Subscribe(ignoreCancelled = true)
    public void normalDeCancelEvent(TestCancellableEvent event) {
      callCount++;
      assertTrue(event.isCancelled());
      event.setCancelled(false);
    }

    @Subscribe(postOrder = PostOrder.LATE)
    public void lateReCancelEvent(TestCancellableEvent event) {
      callCount++;
      assertFalse(event.isCancelled());
      event.setCancelled(true);
    }
  }

  static final class TestBusImpl extends EventBusImpl<Object, TestSubscriberImpl<?>> {
    int createdMethodSubscriptions = 0;
    int createdConsumerSubscriptions = 0;

    @Override
    @SuppressWarnings("unchecked")
    protected <L, T, B extends Subscriber<T>> B makeSubscription(
        Class<T> eventClass,
        Subscribe subscribe,
        L listener,
        BiConsumer<L, T> handler
    ) {
      createdMethodSubscriptions++;
      return (B) new TestSubscriberImpl<>(
          eventClass, subscribe.postOrder(), subscribe.ignoreCancelled(),
          listener, handler
      );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T, B extends Subscriber<T>> B makeSubscription(
        Class<T> eventClass,
        Consumer<T> handler
    ) {
      createdConsumerSubscriptions++;
      return (B) new TestSubscriberImpl<>(eventClass, handler);
    }

    @Override
    public <T> void findSubscriptions(
        @NonNull Object listener,
        TriConsumer<Class<T>, Subscribe, BiConsumer<Object, T>> consumer
    ) {
      super.findSubscriptions(listener, consumer);
    }
  }

  static final class TestSubscriberImpl<E> extends SubscriberImpl<E> {
    public TestSubscriberImpl(Class<E> eventClass, Consumer<E> handler) {
      super(eventClass, handler);
    }

    public <H> TestSubscriberImpl(
        Class<E> eventClass,
        PostOrder postOrder,
        boolean ignoreCancelled,
        H handlerInstance,
        BiConsumer<H, E> handler
    ) {
      super(eventClass, postOrder, ignoreCancelled, handlerInstance, handler);
    }
  }
}
