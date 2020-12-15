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
package org.kitteh.craftirc.messaging;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.craftirc.irc.IRCBot;
import org.kitteh.craftirc.messaging.processing.MessageProcessingStage;
import org.kitteh.craftirc.messaging.processing.PreprocessedMessage;
import org.kitteh.craftirc.messaging.processing.Preprocessor;

public class Minestom2IRC {

    /**
     * Represents a message that can be sent to IRC that should usually originate from
     * Minestom.
     */
    public static class Message {

        protected final @NotNull String user;
        protected final @Nullable String message;
        protected final @NotNull MessageType type;
        protected String formattedMessage;

        public Message(@NotNull String sender, @NotNull String messageContent) {
            user = sender;
            type = MessageType.CHAT;
            message = messageContent;
            formattedMessage = message;
        }

        /**
         * Creates a JOIN/QUIT message
         * @param player The affected user
         * @param join True to indicate join, false to indicate quit
         */
        public Message(@NotNull String player, boolean join) {
            user = player;
            type = join ? MessageType.JOIN : MessageType.QUIT;
            message = null;
            formattedMessage = player + " has " + type.toString();
        }

        public void setFormattedMessage(String newMessage) {
            formattedMessage = newMessage;
        }

        public String getMessage() {
            return formattedMessage;
        }

        public String getUser() {
            return user;
        }

        public MessageType getType() {
            return type;
        }
    }

    /**
     * Abstract interface that can be used to manipulate the contents of the Message
     */
    public static interface Processor {
        public void process(final Message msg);
    }

    private final IRCBot botInstance;
    private final Collection<String> channelNames;

    public Minestom2IRC(IRCBot bot, Collection<String> channels) {
        botInstance = bot;
        channelNames = channels;
    }

    private Set<Preprocessor> earliestProcessors = new HashSet<>();
    private Set<Preprocessor> earlyProcessors = new HashSet<>();
    private Set<Processor> mediumProcessors = new HashSet<>();
    private Set<Processor> lateProcessors = new HashSet<>();

    public void registerProcessor (@NotNull MessageProcessingStage stage, @NotNull Processor processor) {
        switch (stage) {
        case PROCESS:
        case POST_PROCESS:
            throw new IllegalArgumentException("PROCESS and POST_PROCESS are used for preprocessors.");
        case FORMAT:
            mediumProcessors.add(processor);
            break;
        case POSTFORMAT:
            lateProcessors.add(processor);
            break;
        }
    }

    public void registerPreprocessor (@NotNull MessageProcessingStage stage, @NotNull Preprocessor processor) {
        switch (stage) {
        case PROCESS:
            earliestProcessors.add(processor);
            break;
        case POST_PROCESS:
            earlyProcessors.add(processor);
            break;
        case FORMAT:
        case POSTFORMAT:
            throw new IllegalArgumentException("FORMAT and POSTFORMAT are not usable for preprocessors.");
        }
    }

    public void issueMessage(String playername, String messageContent) {
        final PreprocessedMessage preMSG = new PreprocessedMessage(playername, messageContent);
        earliestProcessors.forEach(proc -> proc.preProcess(preMSG));
        earlyProcessors.forEach(proc -> proc.preProcess(preMSG));
        final Message msg = new Message(playername, preMSG.getMessage());
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        channelNames.forEach(channel -> botInstance.sendMessage(channel, msg.getMessage()));
    }

    public void issueJoin(String userName) {
        final Message msg = new Message(userName, true);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        channelNames.forEach(channel -> botInstance.sendMessage(channel, msg.getMessage()));
    }

    public void issueQuit(String userName) {
        final Message msg = new Message(userName, false);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        channelNames.forEach(channel -> botInstance.sendMessage(channel, msg.getMessage()));
    }

    public void addChannel(String channel) {
        channelNames.add(channel);
    }
}
