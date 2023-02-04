/*
 * SkinsRestorer
 *
 * Copyright (C) 2022 SkinsRestorer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.skinsrestorer.shared.listeners;

import co.aikar.commands.CommandManager;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.shared.interfaces.SRProxyAdapter;
import net.skinsrestorer.shared.interfaces.SRProxyPlayer;
import net.skinsrestorer.shared.platform.SRProxyPlugin;
import net.skinsrestorer.shared.storage.SkinStorageImpl;
import net.skinsrestorer.shared.utils.log.SRLogger;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class SRPluginMessageAdapter {
    private final SRLogger logger;
    private final SkinStorageImpl skinStorage;
    private final SRProxyAdapter plugin;
    private final CommandManager<?, ?, ?, ?, ?, ?> manager;

    public void handlePluginMessage(SRPluginMessageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.getTag().equals("sr:messagechannel") && !event.getTag().equals("sr:skinchange")) {
            return;
        }

        if (!event.isServerConnection()) {
            event.setCancelled(true);
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        try {
            String subChannel = in.readUTF();
            Optional<SRProxyPlayer> optional = plugin.getPlayer(in.readUTF());

            if (!optional.isPresent()) {
                return;
            }

            SRProxyPlayer player = optional.get();

            switch (subChannel) {
                // sr:messagechannel
                case "getSkins":
                    int page = Math.min(in.readInt(), 999);
                    SRProxyPlugin.sendPage(page, player, logger, skinStorage);
                    break;
                case "clearSkin":
                    manager.getRootCommand("skin").execute(
                            manager.getCommandIssuer(player.getAs(Object.class)), "skin", new String[]{"clear"});
                    break;
                case "setSkin":
                    String skin = in.readUTF();
                    manager.getRootCommand("skin").execute(
                            manager.getCommandIssuer(player.getAs(Object.class)), "skin", new String[]{"set", skin});
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}