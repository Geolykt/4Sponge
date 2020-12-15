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

