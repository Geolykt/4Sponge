/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
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
import net.minestom.server.event.player.PlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The standard {@link Endpoint} for minecraft chat messages.
 */
@Loadable.Type(name = "mc-chat")
public class ChatEndpoint extends MinecraftEndpoint {
    public ChatEndpoint(@NotNull CraftIRC plugin) {
        super(plugin);
    }

    @Override
    protected void receiveMessage(@NotNull TargetedMessage message) {
        @SuppressWarnings("unchecked")
        Set<MinecraftPlayer> recipients = (Set<MinecraftPlayer>) message.getCustomData().get(ChatEndpoint.RECIPIENT_NAMES);
        for (MinecraftPlayer recipient : recipients) {
            Optional<Player> player = Optional.ofNullable(MinecraftServer.getConnectionManager().getPlayer(recipient.getName()));
            player.ifPresent(pl -> pl.sendMessage(message.getCustomMessage()));
        }
    }

    public void onChat(@NotNull PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        Set<MinecraftPlayer> recipients = this.collectionToMinecraftPlayer(event.getRecipients());
        data.put(ChatEndpoint.RECIPIENT_NAMES, recipients);
        this.getPlugin().getEndpointManager().sendMessage(new Message(this, String.format("<%s> %s", event.getSender().getUsername(), event.getMessage()), data));
    }

    private EventCallback<PlayerChatEvent> callback;

    @Override
    public void registerListener() {
        if (callback == null) {
            callback = (evt) -> {
                this.onChat(evt);
            };
        }
        MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
            if (callback != null) {
                player.addEventCallback(PlayerChatEvent.class, callback);
            }
        });
    }

    @Override
    public void unregisterListeners() {
        for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            p.removeEventCallback(PlayerChatEvent.class, callback);
        }
        callback = null;
    }
}
