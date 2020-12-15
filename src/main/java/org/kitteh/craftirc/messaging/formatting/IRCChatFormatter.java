package org.kitteh.craftirc.messaging.formatting;

import java.util.Collections;

import org.apache.commons.text.StringSubstitutor;
import org.kitteh.craftirc.messaging.Minestom2IRC;

/**
 * The IRC Chat Formatter is a nice formatter that formats messages sent to the IRC.
 */
public class IRCChatFormatter implements Minestom2IRC.Processor {

    private final String chat;
    private final String join;
    private final String quit;

    public IRCChatFormatter(String usingFormatChat, String usingFormatJoin, String usingFormatLeave) {
        chat = usingFormatChat;
        join = usingFormatJoin;
        quit = usingFormatLeave;
    }

    @Override
    public void process(Minestom2IRC.Message msg) {
        switch (msg.getType()) {
        case CHAT:
            msg.setFormattedMessage(new StringSubstitutor(new SubstitutionMap(msg.getUser(), msg.getMessage())).replace(chat));
            break;
        case JOIN:
            msg.setFormattedMessage(new StringSubstitutor(Collections.singletonMap("user", msg.getUser())).replace(join));
            break;
        case QUIT:
            msg.setFormattedMessage(new StringSubstitutor(Collections.singletonMap("user", msg.getUser())).replace(quit));
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

} 