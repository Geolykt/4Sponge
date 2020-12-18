package org.kitteh.craftirc.forge;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ForgeIRCConfig {

    private final ConfigValue<List<? extends String>> botNames;

    public ForgeIRCConfig(@Nonnull ForgeConfigSpec.Builder confBuilder) {
        confBuilder.comment("The main configuration for the ForgeIRC mod").push("forgeirc");
        // The maximum amount of bots could be higher, but there is no need to have more than 10 bots active. Especially as of yet.
        botNames = confBuilder.comment("The bots that should be loaded").defineList("botNames", List.of("default"), (obj) -> true);
        confBuilder.comment("The bots that should be used. Please note the default is always going to be with you").push("bots");
        confBuilder.comment("The default bot").push("default");
        confBuilder.comment("The host the bot should connect to").define("host", "localhost");
        confBuilder.comment("The port the bot should connect to").define("port", 6667);
        confBuilder.comment("True if SSL should be used").define("ssl", false);
        confBuilder.comment("The server password").define("password", "");
        confBuilder.comment("The channel the bot should connect to").define("channel", "#craftirc");

        confBuilder.push("bind");
        confBuilder.comment("The host the bot should bind to").define("host", "0.0.0.0");
        confBuilder.comment("The port the bot should bind to. 0 for any").define("port", 0);
        confBuilder.pop();

        confBuilder.comment("The username that should bot should have").define("user", "CraftIRC");
        confBuilder.comment("The real name that the bot should use").define("realname", "CraftIRC/MinestomIRC Bot");
        confBuilder.comment("The nick the bot should have").define("nick", "ForgeIRC");

        confBuilder.comment("Authentification settings for the bot").push("auth");
        confBuilder.comment("The NickServ user of the bot").define("user", "");
        confBuilder.comment("The NickServ password of the bot").define("pass", "");
        confBuilder.comment("Whether the server is nickless").define("nickless", false);
        confBuilder.pop();

        confBuilder.comment("Debug parameters").push("debug-output");
        confBuilder.define("exceptions", false);
        confBuilder.define("input", false);
        confBuilder.define("output", false);
        confBuilder.pop();

        confBuilder.push("format");
        confBuilder.comment("Format of IRC chat broadcasted to the minecraft server")
            .define("mc-chat", "\u00A74*\u00A7cIRC \u00A7e <${user}>: \u00A7r ${msg}");
        confBuilder.comment("Unused as of now")
            .define("mc-join", "\u00A74*\u00A7cIRC \u00A7e ${user} joined the IRC.");
        confBuilder.comment("Format of IRC Chat broadcasted to the minecraft server")
            .define("mc-quit", "\u00A74*\u00A7cIRC \u00A7e ${user} left the IRC.");
        confBuilder.comment("Format of Minecraft joins broadcasted to IRC.").define("irc-join", "\u00037 ${user} joined.");
        confBuilder.comment("Format of Minecraft disconnections broadcasted to IRC.").define("irc-quit", "\u00037 ${user} left.");
        confBuilder.comment("Format of Minecraft chatter broadcasted to IRC.").define("irc-chat", "\u00037 ${user}: \u0003 ${msg}");
        confBuilder.pop();

        confBuilder.comment("Events that should be listened to").push("event");
        confBuilder.define("mc-chat", true);
        confBuilder.define("mc-join", true);
        confBuilder.define("mc-quit", true);
        confBuilder.define("irc-chat", true);
        confBuilder.comment("Currently unused").define("irc-join", true);
        confBuilder.comment("Currently unused").define("irc-quit", true);
        confBuilder.pop();

        confBuilder.comment("Processors alter the format before it's sent to IRC/MC").push("processors");
        confBuilder.comment("Converts MC colors into IRC Colors").define("colors-irc", true);
        confBuilder.comment("Converts IRC colors into MC Colors").define("colors-mc", true);
        confBuilder.pop(3);
    }

    public final List<? extends String> getBots() {
        return botNames.get();
    }
}
