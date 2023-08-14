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

import org.geysermc.event.Counter;
import org.geysermc.event.TestChildEvent;
import org.geysermc.event.TestEvent;
import org.geysermc.event.subscribe.Subscribe;

final class TestEventListener {
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