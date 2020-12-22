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
package org.kitteh.craftirc.irc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.event.IRCEventListener;
import org.kitteh.craftirc.event.MinestomEventListener;
import org.kitteh.craftirc.messaging.formatting.IRCChatFormatter;
import org.kitteh.craftirc.messaging.formatting.MinestomChatFormatter;
import org.kitteh.craftirc.messaging.processing.IRCColor;
import org.kitteh.craftirc.messaging.processing.MessageProcessingStage;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.Client.Builder.Server.SecurityType;
import org.kitteh.irc.client.library.feature.auth.NickServ;
import org.spongepowered.configurate.ConfigurationNode;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages IRC bots.
 */
public final class BotManager {
    private final Map<String, IRCBot> bots = new ConcurrentHashMap<>();
    public final Map<String, MinestomEventListener> listeners = new ConcurrentHashMap<>();

    /**
     * Initialised by {@link CraftIRC} main.
     *
     * @param bots list of bot data to load
     */
    public BotManager(@NotNull List<? extends ConfigurationNode> bots) {
        this.loadBots(bots);
    }

    public void shutdown() {
        bots.forEach((name, bot) -> bot.shutdown());
    }

    /**
     * Obtains a set of entries of the bots that are known to the BotManager.
     * The Key of the entries is the name of the bot, the value the bot itself.
     * @return The bots known to the BotManager
     */
    public final Set<Map.Entry<String, IRCBot>> getBots() {
        return bots.entrySet();
    }

    /**
     * Gets a bot by name.
     *
     * @param name bot name
     * @return named bot or null if no such bot exists
     */
    @Nullable
    public IRCBot getBot(@NotNull String name) {
        return this.bots.get(name);
    }

    private void loadBots(@NotNull List<? extends ConfigurationNode> list) {
        Set<String> usedBotNames = new HashSet<>();
        int nonMap = 0;
        int noName = 0;
        for (final ConfigurationNode node : list) {
            if (!node.isMap()) {
                nonMap++;
                continue;
            }
            final String name = node.node("name").getString();
            if (name == null) {
                noName++;
                continue;
            }
            if (!usedBotNames.add(name)) {
                CraftIRC.log().warn(String.format("Ignoring duplicate bot with name %s", name));
                continue;
            }
            this.addBot(name, node);
        }
        if (nonMap > 0) {
            CraftIRC.log().warn(String.format("Bots list contained %d entries which were not maps", nonMap));
        }
        if (noName > 0) {
            CraftIRC.log().warn(String.format("Bots list contained %d entries without a 'name'", noName));
        }
    }

    private void addBot(@NotNull String name, @NotNull ConfigurationNode data) {
        Client.Builder botBuilder = Client.builder();
        botBuilder.name(name);
        botBuilder.server().host(data.node("host").getString("localhost"));
        SecurityType security = data.node("ssl").getBoolean() ? SecurityType.SECURE : SecurityType.INSECURE;
        botBuilder.server().port(data.node("port").getInt(6667), security);
        botBuilder.server().password(data.node("password").getString());
        botBuilder.user(data.node("user").getString("CraftIRC"));
        botBuilder.realName(data.node("realname").getString("CraftIRC Bot"));
        botBuilder.nick(data.node("nick").getString("CraftIRC"));

        ConfigurationNode bind = data.node("bind");
        botBuilder.bind().host(bind.node("host").getString());
        botBuilder.bind().port(bind.node("port").getInt(0));

        ConfigurationNode auth = data.node("auth");
        String authUser = auth.node("user").getString();
        String authPass = auth.node("pass").getString();
        boolean nickless = auth.node("nickless").getBoolean();

        ConfigurationNode debug = data.node("debug-output");
        if (debug.node("exceptions").getBoolean()) {
            botBuilder.listeners().exception(exception -> CraftIRC.log().warn("Exception on bot " + name, exception));
        } else {
            botBuilder.listeners().exception(null);
        }
        if (debug.node("input").getBoolean()) {
            botBuilder.listeners().input(input -> CraftIRC.log().info("[IN] " + input));
        }
        if (debug.node("output").getBoolean()) {
            botBuilder.listeners().output(output -> CraftIRC.log().info("[OUT] " + output));
        }

        Client newBot = botBuilder.build();

        if (authUser != null && authPass != null) {
            newBot.getAuthManager().addProtocol(nickless ? new NicklessServ(newBot, authUser, authPass) : NickServ.builder(newBot).account(authUser).password(authPass).build());
        }

        newBot.connect();

        final IRCBot bot = new IRCBot(name, newBot);

        ConfigurationNode events = data.node("event");
        ConfigurationNode format = data.node("format");
        ConfigurationNode processors = data.node("processors");

        // register IRC events
        bot.getClient().getEventManager().registerEventListener(new IRCEventListener(bot.getToMinestom(), 
                events.node("irc-chat").getBoolean(),
                events.node("irc-join").getBoolean(),
                events.node("irc-quit").getBoolean(),
                events.node("irc-kick").getBoolean(),
                events.node("irc-away").getBoolean()));

        // register minecraft events
        MinestomEventListener mcEvents = new MinestomEventListener(bot.getToIRC());
        if (events.node("mc-chat").getBoolean()) {
            MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
                player.addEventCallback(PlayerChatEvent.class, mcEvents::onPlayerChat);
            });
        }
        if (events.node("mc-join").getBoolean()) {
            MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
                player.addEventCallback(PlayerLoginEvent.class, mcEvents::onPlayerJoin);
            });
        }
        if (events.node("mc-quit").getBoolean()) {
            MinecraftServer.getConnectionManager().addPlayerInitialization(player -> {
                player.addEventCallback(PlayerDisconnectEvent.class, mcEvents::onPlayerLeave);
            });
        }

        // register formatters
        bot.getToIRC().registerProcessor(MessageProcessingStage.FORMAT, 
                new IRCChatFormatter(format.node("irc-chat").getString(), 
                        format.node("irc-join").getString(), 
                        format.node("irc-quit").getString()));
        bot.getToMinestom().registerProcessor(MessageProcessingStage.FORMAT, 
                new MinestomChatFormatter(format.node("mc-chat").getString(), 
                        format.node("mc-join").getString(), 
                        format.node("mc-quit").getString(),
                        format.node("mc-kick").getString(format.node("mc-quit").getString()),
                        format.node("mc-away").getString(format.node("mc-quit").getString()),
                        format.node("mc-back").getString(format.node("mc-join").getString())));

        // register preprocessors
        if (processors.node("colors-irc").getBoolean()) {
            bot.getToIRC().registerPreprocessor(MessageProcessingStage.PROCESS, new IRCColor(true));
        }
        if (processors.node("colors-mc").getBoolean()) {
            bot.getToMinestom().registerPreprocessor(MessageProcessingStage.PROCESS, new IRCColor(false));
        }

        // Add bot to channel
        bot.addChannel(data.node("channel").getString());

        // register bot
        this.bots.put(name, bot);
    }
}
