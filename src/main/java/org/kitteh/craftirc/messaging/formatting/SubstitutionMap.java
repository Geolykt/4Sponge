/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
 * * Copyright (C) 2020 Emeric Werner https://geolykt.de
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.craftirc.messaging.formatting;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Sets;

/**
 * Internal helper class for speed!
 */
public final class SubstitutionMap implements Map<String, String> {
    private final String user;
    private final String message;

    protected SubstitutionMap(@NotNull String userName, @NotNull String messageContent) {
        user = userName;
        message = messageContent;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        // It's written that way for null safety
        return "user".equals(key) || "msg".equals(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return user.equals(value) || message.equals(value);
    }

    @Override
    public String get(Object key) {
        // It's written that way for null safety
        if ("user".equals(key)) {
            return user;
        } else if ("msg".equals(key)) {
            return message;
        } else {
            return null;
        }
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("Cannot modify the map.");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("Cannot modify the map.");
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("Cannot modify the map.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot modify the map.");
    }

    @Override
    public Set<String> keySet() {
        return Sets.newHashSet("user", "msg");
    }

    @Override
    public Collection<String> values() {
        return Sets.newHashSet(user, message);
    }

    @SuppressWarnings("unchecked") // It's safe, don't worry
    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return Sets.newHashSet(new AbstractMap.SimpleEntry<String, String>("user", user),
                new AbstractMap.SimpleEntry<String, String>("msg", message));
    }
}

