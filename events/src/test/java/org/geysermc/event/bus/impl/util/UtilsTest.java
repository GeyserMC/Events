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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import org.geysermc.event.Cancellable;
import org.geysermc.event.Event;
import org.geysermc.event.util.AbstractCancellable;
import org.junit.jupiter.api.Test;

public class UtilsTest {
    @Test
    void implEventAncestors() {
        validateAncestors(TestEvent.class, Event.class, TestEvent.class);
    }

    @Test
    void childImplEventAncestors() {
        validateAncestors(
                TestChildEvent.class, Event.class,
                TestChildEvent.class, TestEvent.class);
    }

    @Test
    void abstractCancellableImplEventAncestors() {
        validateAncestors(AbstractCancellableTestEvent.class, Event.class, AbstractCancellableTestEvent.class);
    }

    @Test
    void abstractCancellableChildImplEventAncestors() {
        validateAncestors(
                AbstractCancellableChildTestEvent.class, Event.class,
                AbstractCancellableChildTestEvent.class, TestEvent.class);
    }

    @Test
    void eventImplAncestors() {
        validateAncestors(
                ITestEventImpl.class, Event.class,
                ITestEventImpl.class, ITestEvent.class);
    }

    @Test
    void childEventImplAncestors() {
        validateAncestors(
                ITestChildEventImpl.class,
                Event.class,
                ITestChildEventImpl.class,
                ITestChildEvent.class,
                ITestEvent.class);
    }

    @Test
    void abstractCancellableChildICancellableEventAncestors() {
        validateAncestors(
                AbstractCancellableChildICancellableTestEvent.class, Event.class,
                AbstractCancellableChildICancellableTestEvent.class, ICancellableTestEvent.class);
    }

    @Test
    void implAncestors() {
        validateAncestors(
                ITestImpl.class, Object.class,
                ITestImpl.class, ITest.class);
    }

    @Test
    void cancellableImplAncestors() {
        validateAncestors(
                ICancellableTestImpl.class, Object.class,
                ICancellableTestImpl.class, ICancellableTest.class);
    }

    private void validateAncestors(Class<?> event, Class<?> base, Class<?>... expected) {
        // use Set for expected because sets don't validate order
        assertEquals(new HashSet<>(Arrays.asList(expected)), Utils.ancestorsThatUse(event, base));
    }

    // region no interfaces - event
    static class TestEvent implements Event {}

    static class TestChildEvent extends TestEvent {}

    static class AbstractCancellableTestEvent extends AbstractCancellable implements Event {}

    static class AbstractCancellableChildTestEvent extends TestEvent implements Event {}
    // endregion no interfaces - event

    // region with interfaces - event
    interface ITestEvent extends Event {}

    interface ITestChildEvent extends ITestEvent {}

    interface ICancellableTestEvent extends Event, Cancellable {}

    static class ITestEventImpl implements ITestEvent {}

    static class ITestChildEventImpl implements ITestChildEvent {}

    static class AbstractCancellableChildICancellableTestEvent extends AbstractCancellable
            implements ICancellableTestEvent {}
    // endregion with interfaces - event

    // region base class object specific
    interface ITest {}

    static class ITestImpl implements ITest {}

    interface ICancellableTest extends Cancellable {}

    static class ICancellableTestImpl extends AbstractCancellable implements ICancellableTest {}
    // endregion base class object specific
}
