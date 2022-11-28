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
package net.skinsrestorer.shared.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.shared.interfaces.ISRCommandSender;
import net.skinsrestorer.shared.interfaces.ISRProxyPlayer;
import net.skinsrestorer.shared.listeners.SharedPluginMessageListener;
import net.skinsrestorer.shared.storage.CooldownStorage;
import net.skinsrestorer.shared.storage.Message;

@RequiredArgsConstructor
public abstract class SharedProxyGUICommand extends BaseCommand {
    private final CooldownStorage cooldownStorage;
    private final SharedPluginMessageListener pluginMessageListener;

    protected void onHelp(ISRCommandSender sender, CommandHelp help) {
        sender.sendMessage("SkinsRestorer Help");
        help.showHelp();
    }

    protected void onDefault(ISRProxyPlayer player) {
        if (!player.hasPermission("skinsrestorer.bypasscooldown") && cooldownStorage.hasCooldown(player.getName())) {
            player.sendMessage(Message.SKIN_COOLDOWN, String.valueOf(cooldownStorage.getCooldownSeconds(player.getName())));
            return;
        }
        player.sendMessage(Message.SKINSMENU_OPEN);

        pluginMessageListener.sendPage(0, player);
    }
}