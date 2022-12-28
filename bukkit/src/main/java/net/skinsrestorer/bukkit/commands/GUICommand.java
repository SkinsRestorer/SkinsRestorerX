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
package net.skinsrestorer.bukkit.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import net.skinsrestorer.bukkit.SkinsGUI;
import net.skinsrestorer.bukkit.SkinsRestorerBukkit;
import net.skinsrestorer.bukkit.utils.WrapperBukkit;
import net.skinsrestorer.shared.SkinsRestorerLocale;
import net.skinsrestorer.shared.commands.SharedSkinCommand;
import net.skinsrestorer.shared.interfaces.ISRPlayer;
import net.skinsrestorer.shared.storage.CooldownStorage;
import net.skinsrestorer.shared.storage.Message;
import net.skinsrestorer.shared.storage.SkinStorage;
import net.skinsrestorer.shared.utils.log.SRLogger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.inject.Inject;

@CommandAlias("skins")
@CommandPermission("%skins")
@SuppressWarnings({"unused"})
public class GUICommand extends BaseCommand {
    @Inject
    private SkinsRestorerBukkit plugin;
    @Inject
    private CooldownStorage cooldownStorage;
    @Inject
    private SkinsRestorerLocale locale;
    @Inject
    private SRLogger logger;
    @Inject
    private SkinStorage skinStorage;
    @Inject
    private SharedSkinCommand skinCommand;
    @Inject
    private WrapperBukkit wrapper;

    @Default
    @CommandPermission("%skins")
    public void onDefault(ISRPlayer srPlayer) {
        plugin.runAsync(() -> {
            if (!srPlayer.hasPermission("skinsrestorer.bypasscooldown") && cooldownStorage.hasCooldown(srPlayer.getUniqueId())) {
                srPlayer.sendMessage(Message.SKIN_COOLDOWN, cooldownStorage.getCooldownSeconds(srPlayer.getUniqueId()));
                return;
            }
            srPlayer.sendMessage(Message.SKINSMENU_OPEN);

            Inventory inventory = SkinsGUI.createGUI(new SkinsGUI.ServerGUIActions(plugin, skinCommand, locale, logger, plugin.getServer(), skinStorage, wrapper),
                    locale, logger, plugin.getServer(), skinStorage, srPlayer, 0);
            plugin.runSync(() -> srPlayer.getWrapper().get(Player.class).openInventory(inventory));
        });
    }
}
