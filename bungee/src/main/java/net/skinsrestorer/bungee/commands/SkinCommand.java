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

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bungee.contexts.OnlinePlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.skinsrestorer.api.SkinVariant;
import net.skinsrestorer.bungee.utils.WrapperBungee;
import net.skinsrestorer.shared.SkinsRestorerLocale;
import net.skinsrestorer.shared.commands.SharedSkinCommand;
import net.skinsrestorer.shared.interfaces.ISRPlugin;
import net.skinsrestorer.shared.storage.CooldownStorage;
import net.skinsrestorer.shared.storage.SkinStorage;
import net.skinsrestorer.shared.utils.log.SRLogger;

@CommandAlias("skin")
@CommandPermission("%skin")
@SuppressWarnings({"unused"})
public class SkinCommand extends SharedSkinCommand {
    private final WrapperBungee wrapper;

    public SkinCommand(ISRPlugin plugin, SettingsManager settings, CooldownStorage cooldownStorage, SkinStorage skinStorage, SkinsRestorerLocale locale, SRLogger logger, WrapperBungee wrapper) {
        super(plugin, settings, cooldownStorage, skinStorage, locale, logger);
        this.wrapper = wrapper;
    }

    @Default
    public void onDefault(CommandSender sender) {
        onDefault(wrapper.commandSender(sender));
    }

    @Default
    @CommandPermission("%skinSet")
    @Description("%helpSkinSet")
    @Syntax("%SyntaxDefaultCommand")
    public void onSkinSetShort(ProxiedPlayer player, String skin) {
        onSkinSetOther(player, new OnlinePlayer(player), skin, null);
    }

    @HelpCommand
    @Syntax("%helpHelpCommand")
    public void onHelp(CommandSender sender, CommandHelp help) {
        onHelp(wrapper.commandSender(sender), help);
    }

    @Subcommand("clear")
    @CommandPermission("%skinClear")
    @Description("%helpSkinClear")
    public void onSkinClear(ProxiedPlayer player) {
        onSkinClear(wrapper.player(player));
    }

    @Subcommand("clear")
    @CommandPermission("%skinClearOther")
    @CommandCompletion("@players")
    @Syntax("%SyntaxSkinClearOther")
    @Description("%helpSkinClearOther")
    public void onSkinClearOther(CommandSender sender, @Single OnlinePlayer target) {
        onSkinClearOther(wrapper.commandSender(sender), wrapper.player(target.getPlayer()));
    }

    @Subcommand("search")
    @CommandPermission("%skinSearch")
    @Description("%helpSkinSearch")
    @Syntax("%SyntaxSkinSearch")
    public void onSkinSearch(CommandSender sender, String search) {
        onSkinSearch(wrapper.commandSender(sender), search);
    }

    @Subcommand("update")
    @CommandPermission("%skinUpdate")
    @Description("%helpSkinUpdate")
    public void onSkinUpdate(ProxiedPlayer player) {
        onSkinUpdate(wrapper.player(player));
    }

    @Subcommand("update")
    @CommandPermission("%skinUpdateOther")
    @CommandCompletion("@players")
    @Description("%helpSkinUpdateOther")
    @Syntax("%SyntaxSkinUpdateOther")
    public void onSkinUpdateOther(CommandSender sender, @Single OnlinePlayer target) {
        onSkinUpdateOther(wrapper.commandSender(sender), wrapper.player(target.getPlayer()));
    }

    @Subcommand("set")
    @CommandPermission("%skinSet")
    @CommandCompletion("@skin")
    @Description("%helpSkinSet")
    @Syntax("%SyntaxSkinSet")
    public void onSkinSet(ProxiedPlayer player, String[] skin) {
        onSkinSet(wrapper.player(player), skin);
    }

    @Subcommand("set")
    @CommandPermission("%skinSetOther")
    @CommandCompletion("@players @skin")
    @Description("%helpSkinSetOther")
    @Syntax("%SyntaxSkinSetOther")
    public void onSkinSetOther(CommandSender sender, OnlinePlayer target, String skin, @Optional SkinVariant skinVariant) {
        onSkinSetOther(wrapper.commandSender(sender), wrapper.player(target.getPlayer()), skin, skinVariant);
    }

    @Subcommand("url")
    @CommandPermission("%skinSetUrl")
    @CommandCompletion("@skinUrl")
    @Description("%helpSkinSetUrl")
    @Syntax("%SyntaxSkinUrl")
    public void onSkinSetUrl(ProxiedPlayer player, String url, @Optional SkinVariant skinVariant) {
        onSkinSetUrl(wrapper.player(player), url, skinVariant);
    }
}
