package org.kitteh.craftirc.messaging;

import java.util.HashSet;
import java.util.Set;

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
     */
    public static class Message {

        protected final @NotNull String user;
        protected final @Nullable String message;
        protected final @NotNull MessageType type;
        protected @Nullable JsonMessage formattedMessage;

        public Message(@NotNull String sender, @NotNull String content) {
            user = sender;
            type = MessageType.CHAT;
            message = content;
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
        final PreprocessedMessage preMSG = new PreprocessedMessage(playername, messageContent);
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

    public void issueQuit(String userName) {
        final Message msg = new Message(userName, false);
        mediumProcessors.forEach(proc -> proc.process(msg));
        lateProcessors.forEach(proc -> proc.process(msg));
        MinecraftServer.getConnectionManager().broadcastMessage(msg.getMessage());
    }
}
