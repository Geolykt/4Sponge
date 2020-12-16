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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.kitteh.craftirc.messaging.processing.MessageProcessingStage;
import org.kitteh.craftirc.messaging.processing.PreprocessedMessage;
import org.kitteh.craftirc.messaging.processing.Preprocessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

public class IRC2Minestom {

    /**
     * Represents a message that can be sent to Minestom that should usually originate from
     * IRC.
     */
    public static class Message {

        protected final @Nonnull String user;
        protected final @Nullable String message;
        protected final @Nonnull MessageType type;
        protected @Nullable ITextComponent formattedMessage;

        public Message(@Nonnull String sender, @Nonnull String content) {
            user = sender;
            type = MessageType.CHAT;
            message = content;
        }

        /**
         * Creates a JOIN/QUIT message
         * @param player The affected user
         * @param join True to indicate join, false to indicate quit
         */
        public Message(@Nonnull String player, boolean join) {
            user = player;
            type = join ? MessageType.JOIN : MessageType.QUIT;
            message = null;
        }

        public void setFormattedMessage(@Nonnull ITextComponent newMessage) {
            formattedMessage = newMessage;
        }

        public @Nullable ITextComponent getMessage() {
            return formattedMessage;
        }
        
        public @Nullable String getOriginal() {
            return message;
        }

        public @Nonnull String getUser() {
            return user;
        }

        public @Nonnull MessageType getType() {
            return type;
        }
    }

    /**
     * Abstract interface that can be used to manipulate the contents of the Message
     */
    public static interface Processor {
        public void process(final Message msg);
    }

    private final MinecraftServer server;

    public IRC2Minestom(MinecraftServer mcServer) {
        server = mcServer;
    }

    private Set<Preprocessor> earliestProcessors = new HashSet<>();
    private Set<Preprocessor> earlyProcessors = new HashSet<>();
    private Set<Processor> mediumProcessors = new HashSet<>();
    private Set<Processor> lateProcessors = new HashSet<>();

    public void registerProcessor (@Nonnull MessageProcessingStage stage, @Nonnull Processor processor) {
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

    public void registerPreprocessor (@Nonnull MessageProcessingStage stage, @Nonnull Preprocessor processor) {
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
        // TODO check which method we should actually use
        server.getPlayerList().getPlayers().forEach(player -> player.sendStatusMessage(msg.getMessage(), false));
//        server.getPlayerList().sendPacketToAllPlayers(new SChatPacket(msg.getMessage(), ChatType.CHAT, ));
//        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }

    public void issueJoin(String userName) {
        final Message msg = new Message(userName, true);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        server.getPlayerList().getPlayers().forEach(player -> player.sendStatusMessage(msg.getMessage(), false));
//        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }

    public void issueQuit(String userName) {
        final Message msg = new Message(userName, false);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        server.getPlayerList().getPlayers().forEach(player -> player.sendStatusMessage(msg.getMessage(), false));
//        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }
}
