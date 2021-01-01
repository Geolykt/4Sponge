/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
 * * Copyright (C) 2020-2021 Emeric Werner https://geolykt.de
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

import org.kitteh.craftirc.messaging.Minestom2IRC;

/**
 * The IRC Chat Formatter is a nice formatter that formats messages sent to the IRC.
 * @since 5.0.0
 */
public class IRCChatFormatter implements Minestom2IRC.Processor {

    private final String chat;
    private final String join;
    private final String quit;

    /**
     * Creates a new IRCChatFormatter with the given messages.
     * @param usingFormatChat The format of the chat messages
     * @param usingFormatJoin The format of the join messages
     * @param usingFormatDisconnect The format of the disconnect messages
     * @since 5.0.0
     */
    public IRCChatFormatter(String usingFormatChat, String usingFormatJoin, String usingFormatDisconnect) {
        chat = usingFormatChat;
        join = usingFormatJoin;
        quit = usingFormatDisconnect;
    }

    @Override
    public void process(Minestom2IRC.Message msg) {
        switch (msg.getType()) {
        case CHAT:
            msg.setFormattedMessage(chat.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getMessage()));
            break;
        case JOIN:
        case BACK:
            msg.setFormattedMessage(join.replaceAll("\\$\\{user}", msg.getUser()));
            break;
        case KICK:
        case QUIT:
        case AWAY:
            msg.setFormattedMessage(quit.replaceAll("\\$\\{user}", msg.getUser()).replaceAll("\\$\\{msg}", msg.getMessage()));
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

} 