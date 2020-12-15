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

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.extensions.Extension;
import net.minestom.server.utils.time.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.exceptions.CraftIRCUnableToStartException;
import org.kitteh.craftirc.exceptions.CraftIRCWillLeakTearsException;
import org.kitteh.craftirc.irc.BotManager;
import org.slf4j.Logger;
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

public final class CraftIRC extends Extension {

    private static Logger loggy;
    private static final String PERMISSION_RELOAD = "craftirc.reload";

    private File configDir;

    private boolean reloading = false;
    private String version = null;

    @Override
    public void preInitialize() {
        version = this.getDescription().getVersion();
    }

    @Override
    public void initialize() {
        Command mainCommand = new Command("craftirc");
        mainCommand.addSyntax((commandSource, args) -> {
            if (!commandSource.hasPermission(PERMISSION_RELOAD)) {
                return;
            }
            String arg = args.getString("arg");
            switch (arg) {
            case "reload":
                if (this.reloading) {
                    commandSource.sendMessage(ColoredText.of(ChatColor.RED, "CraftIRC reload already in progress"));
                } else {
                    this.reloading = true;
                    commandSource.sendMessage(ColoredText.of(ChatColor.CYAN, "CraftIRC reload scheduled!"));
                    MinecraftServer.getSchedulerManager().buildTask(() -> {
                        this.dontMakeAGrownManCry();
                        this.startMeUp();
                        this.reloading = false;
                    }).delay(1, TimeUnit.TICK);
                }
            default:
                
            }
        }, new ArgumentString("arg"));
        mainCommand.setDefaultExecutor((commandSource, args) -> {
            commandSource.sendMessage(ChatColor.CYAN + "CraftIRC version " + ChatColor.RESET + this.version + ChatColor.CYAN +  " - Powered by Kittens\n"
                    + ChatColor.DARK_CYAN + "Original by mbaxter, ported to minestom by geolykt.");
        });
        MinecraftServer.getCommandManager().register(mainCommand);
    }

    @Override
    public void postInitialize() {
        this.startMeUp();
    }

    @Override
    public void terminate() {
        this.dontMakeAGrownManCry();
    }

    private synchronized void startMeUp() {
        try {
            CraftIRC.loggy = getLogger();
            if (configDir == null) {
                configDir = new File(MinecraftServer.getExtensionManager().getExtensionFolder(), "craftIRC");
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

            this.botManager = new BotManager(bots);
        } catch (Exception e) {
            this.getLogger().error("Uh oh", new CraftIRCUnableToStartException("Could not start CraftIRC!", e));
            this.dontMakeAGrownManCry();
            return;
        }
    }

    private synchronized void dontMakeAGrownManCry() {
        getBotManager().shutdown();
        // And lastly...
        CraftIRC.loggy = null;
    }

    @NotNull
    public static Logger log() {
        if (CraftIRC.loggy == null) {
            throw new CraftIRCWillLeakTearsException();
        }
        return CraftIRC.loggy;
    }

    private BotManager botManager;

    @NotNull
    public BotManager getBotManager() {
        return this.botManager;
    }

    private void saveDefaultConfig(@NotNull File dataFolder) {
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
