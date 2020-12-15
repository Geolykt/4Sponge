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
        reportingInstance.issueMessage(event.getSender().getUsername(), event.getMessage());
    }

    public final void onPlayerJoin(PlayerLoginEvent event) {
        reportingInstance.issueJoin(event.getPlayer().getUsername());
    }

    public final void onPlayerLeave(PlayerDisconnectEvent event) {
        reportingInstance.issueQuit(event.getPlayer().getUsername());
    }
}
