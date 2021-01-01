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
package org.kitteh.craftirc.messaging;

import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.craftirc.messaging.processing.MessageProcessingStage;
import org.kitteh.craftirc.messaging.processing.PreprocessedMessage;
import org.kitteh.craftirc.messaging.processing.Preprocessor;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.JsonMessage;

public class IRC2Minestom {

    /**
     * Represents a message that can be sent to Minestom that should usually originate from
     * IRC.
     * @since 5.0.0
     */
    public static class Message {

        protected final @NotNull String user;
        protected final @Nullable String message;
        protected final @NotNull MessageType type;
        protected @Nullable JsonMessage formattedMessage;

        /**
         * Constructs a message of the given type.
         * @param sender The sender of the message
         * @param content The content of the message
         * @param msgType The type of the message
         * @since 5.0.1
         */
        public Message(@NotNull String sender, @NotNull String content, @NotNull MessageType msgType) {
            user = sender;
            type = msgType;
            message = content;
        }

        /**
         * @deprecated This is constructor is ambiguous, use Message(String, String, MessageType) instead.
         * Constructs a chat message with the given sender and content
         * @param sender The name of the sender of the message
         * @param content The content of the message, should exclude the username
         * @since 5.0.0
         */
        @Deprecated(forRemoval = true, since = "5.0.1")
        public Message(@NotNull String sender, @NotNull String content) {
            user = sender;
            type = MessageType.CHAT;
            message = content;
        }

        /**
         * Creates a JOIN/QUIT message
         * @param nick The affected user
         * @param join True to indicate join, false to indicate quit
         * @since 5.0.0
         */
        public Message(@NotNull String nick, boolean join) {
            user = nick;
            type = join ? MessageType.JOIN : MessageType.QUIT;
            message = join ? null : "Quit";
        }

        public void setFormattedMessage(@NotNull JsonMessage newMessage) {
            formattedMessage = newMessage;
        }

        public @Nullable JsonMessage getMessage() {
            return formattedMessage;
        }
        
        public @Nullable String getOriginal() {
            return message;
        }

        public @NotNull String getUser() {
            return user;
        }

        public @NotNull MessageType getType() {
            return type;
        }
    }

    /**
     * Abstract interface that can be used to manipulate the contents of the Message
     * @since 5.0.0
     */
    public static interface Processor {
        public void process(final Message msg);
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
        final PreprocessedMessage preMSG = new PreprocessedMessage(messageContent, playername);
        earliestProcessors.forEach(proc -> proc.preProcess(preMSG));
        earlyProcessors.forEach(proc -> proc.preProcess(preMSG));
        final Message msg = new Message(playername, preMSG.getMessage());
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }

    public void issueJoin(String userName) {
        final Message msg = new Message(userName, true);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }

    @Deprecated(since = "5.0.1", forRemoval = true)
    public void issueQuit(String userName) {
        try {
            issueQuit(userName, "Quit", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Issues and processes a quit or kick message. The quit message applies both parting (channelwide) and quits (networkwide)
     * @param userName The name of the user that performed the operation
     * @param message The given reason of why this happened. Often it is given by the client to state it's intentions.
     * @param isKick True if the disconnection happened due to a kick, false if it was due to other reasons (part or quit).
     * @throws Exception Any exception that happened during the processing phase
     * @since 5.0.1
     */
    public void issueQuit(@NotNull String userName, @NotNull String message, boolean isKick) throws Exception {
        final Message msg = new Message(userName, message, isKick ? MessageType.KICK : MessageType.QUIT);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }

    /**
     * Issues and processes an away message. May not handle messages where the user returns from the away state.
     * @param userName The user that is marked to be away
     * @since 5.0.1
     */
    public void issueAway(@NotNull String userName) {
        final Message msg = new Message(userName, "", MessageType.AWAY);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }

    /**
     * Issues and processes an away message where as the user
     * @param userName The name of the user that is no longer away
     * @since 5.0.1
     */
    public void issueBack(@NonNull String userName) {
        final Message msg = new Message(userName, "", MessageType.BACK);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }
}
