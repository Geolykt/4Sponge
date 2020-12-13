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
package org.kitteh.craftirc.minestom;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Loadable;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The standard {@link Endpoint} for minecraft quit messages.
 */
@Loadable.Type(name = "mc-quit")
public class QuitEndpoint extends MinecraftEndpoint {
    public QuitEndpoint(@NotNull CraftIRC plugin) {
        super(plugin);
    }

    @Override
    protected void receiveMessage(@NotNull TargetedMessage message) {
        // NOOP
    }

    public void onChat(@NotNull PlayerDisconnectEvent event) {
        Map<String, Object> data = new HashMap<>();
        Set<MinecraftPlayer> recipients = this.collectionToMinecraftPlayer(event.getPlayer().getInstance().getPlayers());
        data.put(QuitEndpoint.SENDER_NAME, event.getPlayer().getUsername());
        data.put(QuitEndpoint.RECIPIENT_NAMES, recipients);
        this.getPlugin().getEndpointManager().sendMessage(new Message(this, event.getPlayer().getUsername() + " left the game", data));
    }

    private EventCallback<PlayerDisconnectEvent> callback;

    @Override
    public void registerListener() {
        if (callback == null) {
            callback = (evt) -> {
                this.onChat(evt);
            };
        }
        MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
            if (callback != null) {
                player.addEventCallback(PlayerDisconnectEvent.class, callback);
            }
        });
    }

    @Override
    public void unregisterListeners() {
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            p.removeEventCallback(PlayerDisconnectEvent.class, callback);
        }
        callback = null;
    }
}
