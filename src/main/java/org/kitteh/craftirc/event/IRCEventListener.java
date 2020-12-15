package org.kitteh.craftirc.event;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.messaging.IRC2Minestom;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

public class IRCEventListener {

    public IRC2Minestom handlingInstance;

    public IRCEventListener(IRC2Minestom irc2m) {
        handlingInstance = irc2m;
    }


    @Handler(delivery = Invoke.Asynchronously)
    public void message(@NotNull ChannelMessageEvent event) {
        handlingInstance.issueMessage(event.getActor().getNick(), event.getMessage());
    }

    // TODO JOIN/QUIT
    // TODO implement Channel CTCP Event

}
