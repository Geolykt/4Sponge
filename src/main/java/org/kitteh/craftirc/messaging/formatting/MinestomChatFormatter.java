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

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.messaging.IRC2Minestom;

import net.minestom.server.chat.ColoredText;

/**
 * The MinestomChatFormatter takes care of message translation of messages sent by IRC
 * that should be sent to the Minestom server
 * @since 5.0.0
 */
public class MinestomChatFormatter implements IRC2Minestom.Processor {

    private final String chat;
    private final String join;
    private final String quit;
    private final String kick;
    private final String away;
    private final String back;

    /**
     * Creates a new MinestomChatFormatter with the given messages.
     * @param usingFormatChat The format of the chat messages
     * @param usingFormatJoin The format of the join messages
     * @param usingFormatPart The format of the parting messages
     * @param usingFormatKick The format of the messages where a user was kicked.
     * @param usingFormatAway The format of the message when a user is marked to be away
     * @param usingFormatBack The format of the message when a user is no longer marked to be away
     * @since 5.0.1
     */
    public MinestomChatFormatter(@NotNull String usingFormatChat, @NotNull String usingFormatJoin, 
            @NotNull String usingFormatPart, @NotNull String usingFormatKick, @NotNull String usingFormatAway,
            @NotNull String usingFormatBack) {
        chat = usingFormatChat;
        join = usingFormatJoin;
        quit = usingFormatPart;
        kick = usingFormatKick;
        away = usingFormatAway;
        back = usingFormatBack;
    }

    /**
     * Creates a new MinestomChatFormatter with the given messages. The kick message will be the same as the part message.
     * The away message will also be the same as the parting message
     * @param usingFormatChat The format of the chat messages
     * @param usingFormatJoin The format of the join messages
     * @param usingFormatPart The format of the kick and parting messages
     * @since 5.0.0
     */
    @Deprecated(forRemoval = true, since = "5.0.1")
    public MinestomChatFormatter(String usingFormatChat, String usingFormatJoin, String usingFormatPart) {
        this(usingFormatChat, usingFormatJoin, usingFormatPart, usingFormatPart, usingFormatPart, usingFormatJoin);
    }

    @Override
    public void process(IRC2Minestom.Message msg) {
        String rawMessage;
        switch (msg.getType()) {
        case CHAT:
            rawMessage = chat.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getOriginal());
            break;
        case JOIN:
            rawMessage = join.replaceAll("\\$\\{user}", msg.getUser());
            break;
        case QUIT:
            rawMessage = quit.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getOriginal());
            break;
        case KICK:
            rawMessage = kick.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getOriginal());
            break;
        case AWAY:
            rawMessage = away.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getOriginal());
            break;
        case BACK:
            rawMessage = back.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getOriginal());
            break;
        default:
            throw new IllegalArgumentException();
        }
        msg.setFormattedMessage(ColoredText.of(rawMessage));
    }

}
