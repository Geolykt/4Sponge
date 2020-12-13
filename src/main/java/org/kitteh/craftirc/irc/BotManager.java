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
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.Client.Builder.Server.SecurityType;
import org.kitteh.irc.client.library.feature.auth.NickServ;
import org.spongepowered.configurate.ConfigurationNode;

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
    private final CraftIRC plugin;

    /**
     * Initialized by {@link CraftIRC} main.
     *
     * @param plugin the CraftIRC instance
     * @param bots list of bot data to load
     */
    public BotManager(@NotNull CraftIRC plugin, @NotNull List<? extends ConfigurationNode> bots) {
        this.plugin = plugin;
        this.plugin.trackShutdownable(() -> BotManager.this.bots.values().forEach(IRCBot::shutdown));
        this.loadBots(bots);
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
        ConfigurationNode password = data.node("password");
        if (!password.virtual()) {
            botBuilder.server().password(password.getString());
        }
        botBuilder.user(data.node("user").getString("CraftIRC"));
        botBuilder.realName(data.node("realname").getString("CraftIRC Bot"));

        ConfigurationNode bind = data.node("bind");
        ConfigurationNode bindHost = bind.node("host");
        if (!bindHost.virtual()) {
            botBuilder.bind().host(bindHost.getString());
        }
        botBuilder.bind().port(bind.node("port").getInt(0));
        botBuilder.nick(data.node("nick").getString("CraftIRC"));

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

        this.bots.put(name, new IRCBot(this.plugin, name, newBot));
    }
}
