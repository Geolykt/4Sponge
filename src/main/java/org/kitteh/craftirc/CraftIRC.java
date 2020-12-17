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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import org.apache.logging.log4j.LogManager;
import org.kitteh.craftirc.commands.ForgeIRCCommand;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.exceptions.CraftIRCUnableToStartException;
import org.kitteh.craftirc.irc.BotManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.annotation.Nonnull;

@Mod("forgeirc")
public final class CraftIRC {

    private static final Logger LOGGY = LogManager.getLogger();
    private static final String PERMISSION_RELOAD = "craftirc.reload";

    private File configDir;

    private boolean reloading = false;

    public CraftIRC() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private final ForgeIRCCommand cmd = new ForgeIRCCommand("5.0.0"); // TODO externalise version

    @SubscribeEvent
    private void setup(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("forgeirc").executes(cmd));
    }

    @SubscribeEvent
    private void setup(final FMLServerStartingEvent event) {
        startMeUp(event.getServer());
    }

    @SubscribeEvent
    public void terminate(final FMLServerStoppingEvent event) {
        this.dontMakeAGrownManCry();
    }

    private synchronized void startMeUp(MinecraftServer server) {
        try {
            if (configDir == null) {
                configDir = new File(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(), "forgeirc");
                configDir.mkdirs();
            }

            File configFile = new File(this.configDir, "config.yml");
            if (!configFile.exists()) {
                log().info("No config.yml found, creating a default configuration.");
                this.saveDefaultConfig(this.configDir);
            }

            YamlConfigurationLoader yamlConfigurationLoader = YamlConfigurationLoader.builder().path(configFile.toPath()).build();
            ConfigurationNode root = yamlConfigurationLoader.load();

            if (root.virtual()) {
                throw new CraftIRCInvalidConfigException("Config doesn't appear valid. Would advise starting from scratch.");
            }

            ConfigurationNode botsNode = root.node("bots");
            List<? extends ConfigurationNode> bots;
            if (botsNode.virtual() || (bots = botsNode.childrenList()).isEmpty()) {
                throw new CraftIRCInvalidConfigException("No bots defined!");
            }

            this.botManager = new BotManager(bots, server);
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

    private void saveDefaultConfig(@Nonnull File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            URL url = this.getClass().getClassLoader().getResource("config.yml");
            if (url == null) {
                log().warn("Could not find a default config to copy!");
                return;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream input = connection.getInputStream();

            File outFile = new File(dataFolder, "config.yml");
            OutputStream output = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = input.read(buffer)) > 0) {
                output.write(buffer, 0, lengthRead);
            }

            output.close();
            input.close();
        } catch (IOException ex) {
            log().error("Exception while saving default config", ex);
        }
    }
}
