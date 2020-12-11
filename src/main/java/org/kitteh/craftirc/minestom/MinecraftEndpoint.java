/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
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
package org.kitteh.craftirc.minestom;

import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract root of Minecraft {$link}endpoints.
 */
public abstract class MinecraftEndpoint extends Endpoint {
    public static final String RECIPIENT_NAMES = "RECIPIENT_NAMES";

    private final CraftIRC plugin;

    public MinecraftEndpoint(@Nonnull CraftIRC plugin) {
        this.plugin = plugin;
        this.plugin.registerEndpoint(this);
    }

    @Nonnull
    protected CraftIRC getPlugin() {
        return this.plugin;
    }

    @Override
    protected void preProcessReceivedMessage(@Nonnull TargetedMessage message) {
        Set<MinecraftPlayer> players = this.collectionToMinecraftPlayer(MinecraftServer.getConnectionManager().getOnlinePlayers());
        message.getCustomData().put(MinecraftEndpoint.RECIPIENT_NAMES, players);
    }

    @Nonnull
    protected Set<MinecraftPlayer> collectionToMinecraftPlayer(@Nonnull Collection<Player> collection) {
        return collection.stream().map(player -> new MinecraftPlayer(((Player) player).getUsername(), ((Player) player).getUuid())).collect(Collectors.toCollection(HashSet::new));
    }

    @Nonnull
    protected Set<MinecraftPlayer> commandSourceIterableToMinecraftPlayer(@Nonnull Iterable<? extends CommandSender> iterable) {
        Set<MinecraftPlayer> set = new HashSet<>();
        iterable.forEach(source -> {
            if (source instanceof Player) {
                set.add(new MinecraftPlayer(((Player) source).getUsername(), ((Player) source).getUuid()));
            }
        });
        return set;
    }
}
