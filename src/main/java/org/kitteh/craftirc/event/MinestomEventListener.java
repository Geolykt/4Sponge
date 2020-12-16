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

import org.kitteh.craftirc.messaging.Minestom2IRC;

import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;

public final class MinestomEventListener {

    private final Minestom2IRC reportingInstance;

    public MinestomEventListener(Minestom2IRC m2irc) {
        reportingInstance = m2irc;
    }

    public final void onPlayerChat(PlayerChatEvent event) {
        // TODO also allow for nicks sometime in the future
        // TODO logger
        try {
            reportingInstance.issueMessage(event.getPlayer().getUsername(), event.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public final void onPlayerJoin(PlayerLoginEvent event) {
        try {
            reportingInstance.issueJoin(event.getPlayer().getUsername());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public final void onPlayerLeave(PlayerDisconnectEvent event) {
        try {
            reportingInstance.issueQuit(event.getPlayer().getUsername());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
