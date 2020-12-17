package org.kitteh.craftirc.forge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ForgeIRCCommand implements Command<CommandSource> {

    public final String ver;

    public ForgeIRCCommand(String version) {
        ver = version;
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(new StringTextComponent(
                  TextFormatting.AQUA + "CraftIRC version " + TextFormatting.RESET + this.ver 
                + TextFormatting.AQUA +  " - Powered by Kittens\n"
                + TextFormatting.DARK_AQUA + "Original by mbaxter, ported to minestom by geolykt."), true);
        return 0;
    }

}
