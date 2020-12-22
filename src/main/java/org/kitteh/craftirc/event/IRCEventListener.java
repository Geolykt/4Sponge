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
package org.kitteh.craftirc.event;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.messaging.IRC2Minestom;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.client.library.event.channel.ChannelKickEvent;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.event.channel.ChannelPartEvent;
import org.kitteh.irc.client.library.event.client.ClientAwayStatusChangeEvent;
import org.kitteh.irc.client.library.event.user.UserQuitEvent;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class IRCEventListener {

    public IRC2Minestom handlingInstance;
    protected final boolean handleChat, handleJoins, handleQuits, handleKicks, handleAways;

    /**
     * Creates a new IRCEventListener that listens only for chat interactions
     * @param irc2m The IRC to Minestom bridge to use
     * @since 5.0.0
     */
    @Deprecated(forRemoval = true, since = "5.0.1")
    public IRCEventListener(IRC2Minestom irc2m) {
        this(irc2m, true, false, false, false, false);
    }

    /**
     * Creates a new IRCEventListener which listens to the given interactions in IRC.
     * @param irc2m The IRC to Minestom bridge to use
     * @param chat True if chat inside the IRC channel should be listened to
     * @param join True if the listener should process channel joins
     * @param part True if the listener should process channel disconnects
     * @param kick True if the listener should process channel kicks
     * @param away True if the listener should process when a user marks himself to be away
     * @since 5.0.1
     */
    public IRCEventListener(IRC2Minestom irc2m, boolean chat, boolean join, boolean part, boolean kick, boolean away) {
        handlingInstance = irc2m;
        handleChat = chat;
        handleJoins = join;
        handleQuits = part;
        handleKicks = kick;
        handleAways = away;
    }

    /**
     * Event handler for user messaging within the IRC client. 
     * Messages from the bot should not be passed to the method however as they would create a deadlock.
     * It is recommended to perform this action asynchronously because why not?
     * @param event The event to pass
     * @since 5.0.0
     */
    @Handler(delivery = Invoke.Asynchronously)
    public void message(@NotNull ChannelMessageEvent event) {
        if (!handleChat) {
            return;
        }
        try {
            handlingInstance.issueMessage(event.getActor().getNick(), event.getMessage());
        } catch (RuntimeException e) {
            // TODO logger
            e.printStackTrace();
        }
    }

    /**
     * Event handler for user connections.
     * It is recommended to perform this action asynchronously because why not?
     * @param event The event to pass
     * @since 5.0.1
     */
    @Handler(delivery = Invoke.Asynchronously)
    public void join(@NotNull ChannelJoinEvent event) {
        if (!handleJoins) {
            return;
        }
        try {
            handlingInstance.issueJoin(event.getActor().getNick());
        } catch (RuntimeException e) {
            // TODO logger
            e.printStackTrace();
        }
    }

    /**
     * Event handler for user parts.
     * It is recommended to perform this action asynchronously because why not?
     * @param event The event to pass
     * @since 5.0.1
     */
    @Handler(delivery = Invoke.Asynchronously)
    public void disconnection(@NotNull ChannelPartEvent event) {
        if (!handleQuits) {
            return;
        }
        try {
            handlingInstance.issueQuit(event.getActor().getNick(), event.getMessage(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Event handler for user kicks.
     * It is recommended to perform this action asynchronously because why not?
     * @param event The event to pass
     * @since 5.0.1
     */
    @Handler(delivery = Invoke.Asynchronously)
    public void kick(@NotNull ChannelKickEvent event) {
        if (!handleKicks) {
            return;
        }
        try {
            handlingInstance.issueQuit(event.getActor().getName(), event.getMessage(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Event handler for user quits (as they left the entire network).
     * It is recommended to perform this action asynchronously because why not?
     * @param event The event to pass
     * @since 5.0.1
     */
    @Handler(delivery = Invoke.Asynchronously)
    public void kick(@NotNull UserQuitEvent event) {
        if (!handleQuits) {
            return;
        }
        try {
            handlingInstance.issueQuit(event.getActor().getNick(), event.getMessage(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @deprecated Does not appear to work
     * Event handler for when a user marks himself to be away.
     * It is recommended to perform this action asynchronously because why not?
     * @param event The event to pass
     * @since 5.0.1
     */
    @Deprecated(forRemoval = false, since = "5.0.1")
    @Handler(delivery = Invoke.Asynchronously)
    public void away(ClientAwayStatusChangeEvent event) {
        if (!handleAways) {
            return;
        }
        try {
            if (event.isNowAway()) {
                handlingInstance.issueAway(event.getClient().getNick());
            } else {
                handlingInstance.issueBack(event.getClient().getNick());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // TODO implement Channel CTCP Event

}
