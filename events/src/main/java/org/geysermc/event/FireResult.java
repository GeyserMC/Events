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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.subscribe.Subscriber;

public class FireResult {
    private static final FireResult OK = new FireResult(Collections.emptyMap());

    private final Map<Subscriber<?>, Throwable> exceptions;

    private FireResult(Map<Subscriber<?>, Throwable> exceptions) {
        this.exceptions = Collections.unmodifiableMap(exceptions);
    }

    public static FireResult ok() {
        return OK;
    }

    public boolean success() {
        return exceptions.isEmpty();
    }

    public Map<Subscriber<?>, Throwable> exceptions() {
        return exceptions;
    }

    public static FireResult resultFor(@NonNull Map<Subscriber<?>, Throwable> exceptions) {
        Objects.requireNonNull(exceptions);
        if (exceptions.isEmpty()) {
            return ok();
        }
        return new FireResult(exceptions);
    }
}
