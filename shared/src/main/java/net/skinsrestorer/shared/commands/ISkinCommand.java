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

import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.interfaces.ISRCommandSender;
import net.skinsrestorer.api.interfaces.ISRPlayer;
import net.skinsrestorer.api.property.IProperty;
import net.skinsrestorer.shared.interfaces.ISRPlugin;
import net.skinsrestorer.shared.storage.Config;
import net.skinsrestorer.shared.storage.CooldownStorage;
import net.skinsrestorer.shared.storage.Locale;
import net.skinsrestorer.shared.utils.C;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static co.aikar.commands.CommandManager.getCurrentCommandManager;

public interface ISkinCommand {
    IProperty emptySkin = SkinsRestorerAPI.getApi().createProperty("textures", "", "");

    @SuppressWarnings("deprecation")
    default void onDefault(ISRCommandSender sender) {
        onHelp(sender, getCurrentCommandManager().generateCommandHelp());
    }

    default void onSkinSetShort(ISRPlayer player, String skin) {
        onSkinSetOther(player, player, skin, null);
    }

    default void onHelp(ISRCommandSender sender, CommandHelp help) {
        if (Config.ENABLE_CUSTOM_HELP) sendHelp(sender);
        else help.showHelp();
    }

    default void onSkinClear(ISRPlayer player) {
        onSkinClearOther(player, player);
    }

    default void onSkinClearOther(ISRCommandSender sender, ISRPlayer target) {
        ISRPlugin plugin = getPlugin();
        plugin.runAsync(() -> {
            final String sName = sender.getName();
            if (!sender.hasPermission("skinsrestorer.bypasscooldown") && CooldownStorage.hasCooldown(sName)) {
                sender.sendMessage(String.format(Locale.SKIN_COOLDOWN, CooldownStorage.getCooldown(sName)));
                return;
            }

            final String pName = target.getName();
            final String skin = getPlugin().getSkinStorage().getDefaultSkinName(pName, true);

            // remove users defined skin from database
            plugin.getSkinStorage().removeSkin(pName);

            if (setSkin(sender, target.getWrapper(), skin, false, true, null)) {
                if (sender == target)
                    sender.sendMessage(Locale.SKIN_CLEAR_SUCCESS);
                else
                    sender.sendMessage(Locale.SKIN_CLEAR_ISSUER.replace("%player", pName));
            }
        });
    }

    default void onSkinUpdate(ISRPlayer player) {
        onSkinUpdateOther(player, player);
    }

    default void onSkinUpdateOther(ISRCommandSender sender, ISRPlayer player) {
        ISRPlugin plugin = getPlugin();
        plugin.runAsync(() -> {
            final String sName = sender.getName();
            if (!sender.hasPermission("skinsrestorer.bypasscooldown") && CooldownStorage.hasCooldown(sName)) {
                sender.sendMessage(String.format(Locale.SKIN_COOLDOWN, CooldownStorage.getCooldown(sName)));
                return;
            }

            final String pName = player.getName();
            java.util.Optional<String> skin = plugin.getSkinStorage().getSkinName(pName);

            try {
                if (skin.isPresent()) {
                    // Filter skinUrl
                    if (skin.get().startsWith(" ")) {
                        sender.sendMessage(Locale.ERROR_UPDATING_URL);
                        return;
                    }

                    if (!plugin.getSkinStorage().updateSkinData(skin.get())) {
                        sender.sendMessage(Locale.ERROR_UPDATING_SKIN);
                        return;
                    }

                } else {
                    // get DefaultSkin
                    skin = java.util.Optional.of(plugin.getSkinStorage().getDefaultSkinName(pName, true));
                }
            } catch (SkinRequestException e) {
                sender.sendMessage(e.getMessage());
                return;
            }

            if (setSkin(sender, player.getWrapper(), skin.get(), false, false, null)) {
                if (sender == player)
                    sender.sendMessage(Locale.SUCCESS_UPDATING_SKIN_OTHER.replace("%player", pName));
                else
                    sender.sendMessage(Locale.SUCCESS_UPDATING_SKIN);
            }
        });
    }

    default void onSkinSet(ISRPlayer player, String[] skin) {
        if (skin.length == 0)
            throw new InvalidCommandArgument(true);

        onSkinSetOther(player, player, skin[0], null);
    }

    default void onSkinSetOther(ISRCommandSender sender, ISRPlayer player, String skin, SkinType skinType) {
        ISRPlugin plugin = getPlugin();
        plugin.runAsync(() -> {
            final String sName = sender.getName();
            final String pName = player.getName();
            if (Config.PER_SKIN_PERMISSIONS && !sender.hasPermission("skinsrestorer.skin." + skin)) {
                if (!sender.hasPermission("skinsrestorer.ownskin") && (sender != player || !skin.equalsIgnoreCase(sName))) { // Todo: Test: sender != player
                    sender.sendMessage(Locale.PLAYER_HAS_NO_PERMISSION_SKIN);
                    return;
                }
            }

            if (setSkin(sender, player.getWrapper(), skin, true, false, skinType) && (sender != player))
                sender.sendMessage(Locale.ADMIN_SET_SKIN.replace("%player", pName));
        });
    }

    default void onSkinSetUrl(ISRPlayer player, String url, SkinType skinType) {
        if (!C.validUrl(url)) {
            player.sendMessage(Locale.ERROR_INVALID_URLSKIN);
            return;
        }

        onSkinSetOther(player, player, url, skinType);
    }

    default void sendHelp(ISRCommandSender sender) {
        if (!Locale.SR_LINE.isEmpty())
            sender.sendMessage(Locale.SR_LINE);

        sender.sendMessage(Locale.CUSTOM_HELP_IF_ENABLED.replace("%ver%", getPlugin().getVersion()));

        if (!Locale.SR_LINE.isEmpty())
            sender.sendMessage(Locale.SR_LINE);
    }

    default void rollback(String pName, String oldSkinName, boolean save) {
        if (save)
            getPlugin().getSkinStorage().setSkinName(pName, oldSkinName);
    }

    // if save is false, we won't save the skin name
    // because default skin names shouldn't be saved as the users custom skin
    default boolean setSkin(ISRCommandSender sender, PlayerWrapper player, String skin, boolean save, boolean clear, SkinType skinType) {
        ISRPlugin plugin = getPlugin();

        if (skin.equalsIgnoreCase("null")) {
            sender.sendMessage(Locale.INVALID_PLAYER.replace("%player", skin));
            return false;
        }

        if (Config.DISABLED_SKINS_ENABLED && !clear && !sender.hasPermission("skinsrestorer.bypassdisabled")
                && Config.DISABLED_SKINS.stream().anyMatch(skin::equalsIgnoreCase)) {
            sender.sendMessage(Locale.SKIN_DISABLED);
            return false;
        }

        final String senderName = sender.getName();
        if (!sender.hasPermission("skinsrestorer.bypasscooldown") && CooldownStorage.hasCooldown(senderName)) {
            sender.sendMessage(Locale.SKIN_COOLDOWN.replace("%s", String.valueOf(CooldownStorage.getCooldown(senderName))));
            return false;
        }

        final String playerName = player.getName();
        final Optional<String> oldSkinName = plugin.getSkinStorage().getSkinName(playerName);
        if (C.validUrl(skin)) {
            if (!sender.hasPermission("skinsrestorer.command.set.url")
                    && !Config.SKIN_WITHOUT_PERM
                    && !clear) { // Ignore /skin clear when defaultSkin = url
                sender.sendMessage(Locale.PLAYER_HAS_NO_PERMISSION_URL);
                return false;
            }

            if (!C.allowedSkinUrl(skin)) {
                sender.sendMessage(Locale.SKINURL_DISALLOWED);
                return false;
            }

            // Apply cooldown to sender
            CooldownStorage.setCooldown(senderName, Config.SKIN_CHANGE_COOLDOWN, TimeUnit.SECONDS);

            try {
                sender.sendMessage(Locale.MS_UPDATING_SKIN);
                String skinEntry = " " + playerName; // so won't overwrite premium player names
                if (skinEntry.length() > 16) // max len of 16 char
                    skinEntry = skinEntry.substring(0, 16);

                IProperty generatedSkin = SkinsRestorerAPI.getApi().genSkinUrl(skin, String.valueOf(skinType));
                plugin.getSkinStorage().setSkinData(skinEntry, generatedSkin,
                        System.currentTimeMillis() + Duration.of(100, ChronoUnit.YEARS).toMillis()); // "generate" and save skin for 100 years
                plugin.getSkinStorage().setSkinName(playerName, skinEntry); // set player to "whitespaced" name then reload skin
                SkinsRestorerAPI.getApi().applySkin(player, generatedSkin);

                if (!Locale.SKIN_CHANGE_SUCCESS.isEmpty() && !Locale.SKIN_CHANGE_SUCCESS.equals(Locale.PREFIX))
                    player.sendMessage(Locale.SKIN_CHANGE_SUCCESS.replace("%skin", "skinUrl"));

                return true;
            } catch (SkinRequestException e) {
                sender.sendMessage(e.getMessage());
            } catch (Exception e) {
                plugin.getSrLogger().debug("[ERROR] Exception: could not generate skin url:" + skin + "\nReason= " + e.getMessage());
                sender.sendMessage(Locale.ERROR_INVALID_URLSKIN);
            }
        } else {
            // If skin is not an url, it's a username
            try {
                if (save)
                    plugin.getSkinStorage().setSkinName(playerName, skin);
                // TODO: #getSkinForPlayer() is nested and on different places around bungee/sponge/velocity
                SkinsRestorerAPI.getApi().applySkin(player, skin);

                if (!Locale.SKIN_CHANGE_SUCCESS.isEmpty() && !Locale.SKIN_CHANGE_SUCCESS.equals(Locale.PREFIX))
                    player.sendMessage(Locale.SKIN_CHANGE_SUCCESS.replace("%skin", skin)); // TODO: should this not be sender? -> hidden skin set?

                return true;
            } catch (SkinRequestException e) {
                if (clear) {
                    clearSkin(player);

                    return true;
                }
                sender.sendMessage(e.getMessage());
            }
        }

        // set CoolDown to ERROR_COOLDOWN and rollback to old skin on exception
        CooldownStorage.setCooldown(senderName, Config.SKIN_ERROR_COOLDOWN, TimeUnit.SECONDS);
        rollback(playerName, oldSkinName.orElse(playerName), save);
        return false;
    }

    ISRPlugin getPlugin();

    void clearSkin(PlayerWrapper player);

    enum SkinType {
        STEVE,
        SLIM,
    }
}
