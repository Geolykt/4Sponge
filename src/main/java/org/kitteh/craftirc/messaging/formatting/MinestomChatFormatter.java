package org.kitteh.craftirc.messaging.formatting;

import java.util.Collections;

import org.apache.commons.text.StringSubstitutor;
import org.kitteh.craftirc.messaging.IRC2Minestom;

import net.minestom.server.chat.ColoredText;

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
        String legacyString;
        switch (msg.getType()) {
        case CHAT:
            legacyString = new StringSubstitutor(new SubstitutionMap(msg.getUser(), msg.getOriginal())).replace(chat);
            break;
        case JOIN:
            legacyString = new StringSubstitutor(Collections.singletonMap("user", msg.getUser())).replace(join);
            break;
        case QUIT:
            legacyString = new StringSubstitutor(Collections.singletonMap("user", msg.getUser())).replace(quit);
            break;
        default:
            throw new IllegalArgumentException();
        }
        msg.setFormattedMessage(ColoredText.ofLegacy(legacyString, 'f'));
    }

}
