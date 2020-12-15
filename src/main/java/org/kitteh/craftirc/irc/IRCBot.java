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
package org.kitteh.craftirc.irc;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.messaging.IRC2Minestom;
import org.kitteh.craftirc.messaging.Minestom2IRC;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;

/**
 * Wraps an IRC client and handles events.
 */
public final class IRCBot {
    private final Client client;
    private final String name;

    private final IRC2Minestom minestom;
    private final Minestom2IRC irc;

    IRCBot(@NotNull String name, final @NotNull Client client) {
        this.client = client;
        this.name = name;

        minestom = new IRC2Minestom();
        irc = new Minestom2IRC(this, new LinkedList<>());

    }

    /**
     * Gets the bot's name.
     *
     * @return bot name
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Adds a channel to the bot, which will join when possible.
     *
     * @param channel channel to join
     */
    public void addChannel(@NotNull String channel) {
        this.client.addChannel(channel);
        this.irc.addChannel(channel);
    }

    /**
     * Sends a message to the named channel.
     *
     * @param target target channel
     * @param message message to send
     */
    public void sendMessage(@NotNull Channel target, @NotNull String message) {
        this.client.sendMessage(target.getName(), message);
    }

    /**
     * Sends a message to the named target.
     *
     * @param target target
     * @param message message to send
     */
    public void sendMessage(@NotNull String target, @NotNull String message) {
        this.client.sendMessage(target, message);
    }

    void shutdown() {
        this.client.shutdown("CraftIRC shutting down!");
    }
    
    public final Minestom2IRC getToIRC() {
        return irc;
    }

    public final IRC2Minestom getToMinestom() {
        return minestom;
    }

    protected final Client getClient() {
        return client;
    }
}
