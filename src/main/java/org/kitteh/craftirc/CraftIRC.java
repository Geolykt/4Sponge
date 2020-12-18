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
package org.kitteh.craftirc;

import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.exceptions.CraftIRCUnableToStartException;
import org.kitteh.craftirc.forge.ForgeIRCCommand;
import org.kitteh.craftirc.forge.ForgeIRCConfig;
import org.kitteh.craftirc.irc.BotManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

@Mod("forgeirc")
public final class CraftIRC {

    private static final Logger LOGGY = LogManager.getLogger();

    private final Map.Entry<ForgeIRCConfig, ForgeConfigSpec> modConfig = new ForgeConfigSpec.Builder().configure(ForgeIRCConfig::new);

    public CraftIRC() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, modConfig.getValue(), "forgeirc.toml");
        MinecraftForge.EVENT_BUS.register(this);
    }

    private final ForgeIRCCommand cmd = new ForgeIRCCommand("5.0.0"); // TODO externalise version

    @SubscribeEvent
    public void setup(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("forgeirc").executes(cmd));
    }

    @SubscribeEvent
    public void setup(final FMLServerStartingEvent event) {
        startMeUp(event.getServer());
    }

    @SubscribeEvent
    public void terminate(final FMLServerStoppingEvent event) {
        this.dontMakeAGrownManCry();
    }

    private synchronized void startMeUp(MinecraftServer server) {
        try {
            ForgeConfigSpec config = modConfig.getValue();

            List<? extends String> bots = modConfig.getKey().getBots();
            if (bots.isEmpty()) {
                throw new CraftIRCInvalidConfigException("No bots defined!");
            }

            this.botManager = new BotManager(bots, server, config);
        } catch (Exception e) {
            LOGGY.error("Uh oh", new CraftIRCUnableToStartException("Could not start CraftIRC!", e));
            this.dontMakeAGrownManCry();
            return;
        }
    }

    private synchronized void dontMakeAGrownManCry() {
        getBotManager().shutdown();
    }

    @Nonnull
    public static Logger log() {
        return LOGGY;
    }

    private BotManager botManager;

    @Nonnull
    public BotManager getBotManager() {
        return this.botManager;
    }
}
