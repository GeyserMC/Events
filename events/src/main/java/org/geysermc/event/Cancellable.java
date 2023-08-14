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
package org.geysermc.event;

/**
 * Represents a cancellable event.
 * @since 1.0
 */
public interface Cancellable {
    /**
     * Returns whether the event is cancelled.
     * @since 1.2
     */
    boolean cancelled();

    /**
     * Set whether the event is cancelled.
     *
     * @param cancelled if the event is cancelled
     * @since 1.2
     */
    void cancelled(boolean cancelled);

    /**
     * Returns whether the event is cancelled.
     *
     * @deprecated Replaced with {@link #cancelled()}
     * @since 1.0
     */
    @Deprecated
    boolean isCancelled();

    /**
     * Set whether the event is cancelled.
     *
     * @deprecated Replaced with {@link #cancelled()}
     * @param cancelled if the event is cancelled
     * @since 1.0
     */
    @Deprecated
    void setCancelled(boolean cancelled);
}
