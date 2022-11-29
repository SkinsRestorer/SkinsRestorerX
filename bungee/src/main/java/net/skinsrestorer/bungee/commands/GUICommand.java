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
package net.skinsrestorer.bungee.commands;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.bungee.utils.WrapperBungee;
import net.skinsrestorer.shared.commands.SharedProxyGUICommand;
import net.skinsrestorer.shared.listeners.SharedPluginMessageListener;
import net.skinsrestorer.shared.storage.CooldownStorage;

@CommandAlias("skins")
@CommandPermission("%skins")
@SuppressWarnings({"unused"})
public class GUICommand extends SharedProxyGUICommand {
    private final WrapperBungee wrapper;

    public GUICommand(CooldownStorage cooldownStorage, SharedPluginMessageListener pluginMessageListener, WrapperBungee wrapper) {
        super(cooldownStorage, pluginMessageListener);
        this.wrapper = wrapper;
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        onHelp(wrapper.commandSender(sender), help);
    }

    @Default
    @CommandPermission("%skins")
    public void onDefault(ProxiedPlayer player) {
        onDefault(wrapper.player(player));
    }
}
