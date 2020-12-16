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

import org.kitteh.craftirc.messaging.IRC2Minestom;

import net.minecraft.util.text.StringTextComponent;

public class MinestomChatFormatter implements IRC2Minestom.Processor {

    private final String chat;
    private final String join;
    private final String quit;

    public MinestomChatFormatter(String usingFormatChat, String usingFormatJoin, String usingFormatLeave) {
        chat = usingFormatChat;
        join = usingFormatJoin;
        quit = usingFormatLeave;
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
            rawMessage = quit.replaceAll("\\$\\{user}", msg.getUser());
            break;
        default:
            throw new IllegalArgumentException();
        }
        msg.setFormattedMessage(new StringTextComponent(rawMessage));
    }

}
