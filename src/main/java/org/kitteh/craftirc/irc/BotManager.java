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

import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.event.IRCEventListener;
import org.kitteh.craftirc.event.ForgeEventListener;
import org.kitteh.craftirc.messaging.formatting.IRCChatFormatter;
import org.kitteh.craftirc.messaging.formatting.MinestomChatFormatter;
import org.kitteh.craftirc.messaging.processing.IRCColor;
import org.kitteh.craftirc.messaging.processing.MessageProcessingStage;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.Client.Builder.Server.SecurityType;
import org.kitteh.irc.client.library.feature.auth.NickServ;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages IRC bots.
 */
public final class BotManager {
    private final Map<String, IRCBot> bots = new ConcurrentHashMap<>();
    public final Map<String, ForgeEventListener> listeners = new ConcurrentHashMap<>();

    /**
     * Initialised by {@link CraftIRC} main.
     *
     * @param bots list of bot data to load
     */
    public BotManager(@Nonnull List<? extends String> bots, @Nonnull MinecraftServer server, @Nonnull ForgeConfigSpec conf) {
        this.loadBots(bots, server, conf);
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
    public IRCBot getBot(@Nonnull String name) {
        return this.bots.get(name);
    }

    private void loadBots(@Nonnull List<? extends String> list, @Nonnull MinecraftServer server, @Nonnull ForgeConfigSpec conf) {
        Set<String> usedBotNames = new HashSet<>();
        for (final String node : list) {
            final String name = node;
            //;
            if (!usedBotNames.add(name)) {
                CraftIRC.log().warn(String.format("Ignoring duplicate bot with name %s", name));
                continue;
            }
            this.addBot(name, conf, server);
        }
    }

    @SuppressWarnings("unchecked")
    private void addBot(@Nonnull String name, @Nonnull ForgeConfigSpec config, @Nonnull MinecraftServer mcServer) {
        Client.Builder botBuilder = Client.builder();
        botBuilder.name(name);
        UnmodifiableConfig conf = config.getValues();
        botBuilder.server().host(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".host")).get());
        SecurityType security = ((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".ssl")).get() ? SecurityType.SECURE : SecurityType.INSECURE;
        botBuilder.server().port((int) ((ConfigValue<Number>)conf.get("forgeirc.bots." + name + ".port")).get(), security);
        botBuilder.server().password(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".password")).get());
        botBuilder.user(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".user")).get());
        botBuilder.realName(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".realname")).get());
        botBuilder.nick(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".nick")).get());

        botBuilder.bind().host(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".bind.host")).get());
        botBuilder.bind().port((int) ((ConfigValue<Number>)conf.get("forgeirc.bots." + name + ".bind.port")).get());

        String authUser = ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".auth.user")).get();
        String authPass = ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".auth.pass")).get();
        boolean nickless = ((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".auth.nickless")).get();

        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".debug-output.exceptions")).get()) {
            botBuilder.listeners().exception(exception -> CraftIRC.log().warn("Exception on bot " + name, exception));
        } else {
            botBuilder.listeners().exception(null);
        }
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".debug-output.input")).get()) {
            botBuilder.listeners().input(input -> CraftIRC.log().info("[IN] " + input));
        }
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".debug-output.output")).get()) {
            botBuilder.listeners().output(output -> CraftIRC.log().info("[OUT] " + output));
        }

        Client newBot = botBuilder.build();

        if (authUser != null && authPass != null && !authPass.equals("") && !authUser.equals("")) {
            newBot.getAuthManager().addProtocol(nickless ? new NicklessServ(newBot, authUser, authPass) : NickServ.builder(newBot).account(authUser).password(authPass).build());
        }

        newBot.connect();

        final IRCBot bot = new IRCBot(name, newBot, mcServer);

        // register IRC events
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".event.irc-chat")).get()) {
            bot.getClient().getEventManager().registerEventListener(new IRCEventListener(bot.getToMinestom()));
        }

        // register minecraft events
        ForgeEventListener mcEvents = new ForgeEventListener(bot.getToIRC());
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".event.mc-chat")).get()) {
            MinecraftForge.EVENT_BUS.addListener(mcEvents::onPlayerChat);
        }
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".event.mc-join")).get()) {
            MinecraftForge.EVENT_BUS.addListener(mcEvents::onPlayerJoin);
        }
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".event.mc-quit")).get()) {
            MinecraftForge.EVENT_BUS.addListener(mcEvents::onPlayerLeave);
        }

        // register formatters
        bot.getToIRC().registerProcessor(MessageProcessingStage.FORMAT, 
                new IRCChatFormatter(
                        ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".format.irc-chat")).get(),
                        ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".format.irc-join")).get(),
                        ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".format.irc-quit")).get()));
        bot.getToMinestom().registerProcessor(MessageProcessingStage.FORMAT, 
                new MinestomChatFormatter(
                        ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".format.mc-chat")).get(),
                        ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".format.mc-join")).get(),
                        ((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".format.mc-quit")).get()));

        // register preprocessors
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".processors.colors-irc")).get()) {
            bot.getToIRC().registerPreprocessor(MessageProcessingStage.PROCESS, new IRCColor(true));
        }
        if (((ConfigValue<Boolean>)conf.get("forgeirc.bots." + name + ".processors.colors-mc")).get()) {
            bot.getToMinestom().registerPreprocessor(MessageProcessingStage.PROCESS, new IRCColor(false));
        }

        // Add bot to channel
        bot.addChannel(((ConfigValue<String>)conf.get("forgeirc.bots." + name + ".channel")).get());

        // register bot
        this.bots.put(name, bot);
    }
}
