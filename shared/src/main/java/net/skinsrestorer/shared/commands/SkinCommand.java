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

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.SkinVariant;
import net.skinsrestorer.api.exception.NotPremiumException;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.interfaces.MineSkinAPI;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.shared.SkinsRestorerLocale;
import net.skinsrestorer.shared.acf.OnlineSRPlayer;
import net.skinsrestorer.shared.api.SharedSkinApplier;
import net.skinsrestorer.shared.config.CommandConfig;
import net.skinsrestorer.shared.interfaces.SRCommandSender;
import net.skinsrestorer.shared.interfaces.SRPlatformAdapter;
import net.skinsrestorer.shared.interfaces.SRPlayer;
import net.skinsrestorer.shared.platform.SRPlugin;
import net.skinsrestorer.shared.storage.CooldownStorage;
import net.skinsrestorer.shared.storage.Message;
import net.skinsrestorer.shared.storage.SkinStorageImpl;
import net.skinsrestorer.shared.utils.C;
import net.skinsrestorer.shared.utils.log.SRLogLevel;
import net.skinsrestorer.shared.utils.log.SRLogger;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.skinsrestorer.shared.utils.SharedMethods.getRootCause;

@SuppressWarnings("unused")
@CommandAlias("skin")
@CommandPermission("%skin")
@Conditions("allowed-server")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class SkinCommand extends BaseCommand {
    private final SRPlatformAdapter adapter;
    private final SRPlugin plugin;
    private final SettingsManager settings;
    private final CooldownStorage cooldownStorage;
    private final SkinStorageImpl skinStorage;
    private final SkinsRestorerLocale locale;
    private final SRLogger logger;
    private final SharedSkinApplier<Object> skinApplier;
    private final MineSkinAPI mineSkinAPI;

    @SuppressWarnings("deprecation")
    @Default
    private void onDefault(SRCommandSender sender) {
        onHelp(sender, getCurrentCommandManager().generateCommandHelp());
    }

    @Default
    @CommandPermission("%skinSet")
    @Description("%helpSkinSet")
    @Syntax("%SyntaxDefaultCommand")
    @Conditions("cooldown")
    private void onSkinSetShort(SRPlayer player, String skin) {
        onSkinSetOther(player, new OnlineSRPlayer(player), skin, null);
    }

    @HelpCommand
    @Syntax("%helpHelpCommand")
    private void onHelp(SRCommandSender sender, CommandHelp help) {
        if (settings.getProperty(CommandConfig.ENABLE_CUSTOM_HELP)) {
            sendHelp(sender);
        } else {
            help.showHelp();
        }
    }

    @Subcommand("clear")
    @CommandPermission("%skinClear")
    @Description("%helpSkinClear")
    @Conditions("cooldown")
    private void onSkinClear(SRPlayer player) {
        onSkinClearOther(player, new OnlineSRPlayer(player));
    }

    @Subcommand("clear")
    @CommandPermission("%skinClearOther")
    @CommandCompletion("@players")
    @Syntax("%SyntaxSkinClearOther")
    @Description("%helpSkinClearOther")
    @Conditions("cooldown")
    private void onSkinClearOther(SRCommandSender sender, OnlineSRPlayer target) {
        SRPlayer targetPlayer = target.getPlayer();

        adapter.runAsync(() -> {
            String playerName = targetPlayer.getName();

            // remove users defined skin from database
            skinStorage.removeSkinOfPlayer(playerName);

            try {
                SkinProperty property = skinStorage.getDefaultSkinForPlayer(playerName).getLeft();
                skinApplier.applySkin(targetPlayer.getAs(Object.class), property);
            } catch (NotPremiumException e) {
                skinApplier.applySkin(targetPlayer.getAs(Object.class), SkinProperty.of("", ""));
            } catch (SkinRequestException e) {
                sender.sendMessage(getRootCause(e).getMessage());
            }

            if (sender.getName().equals(targetPlayer.getName())) {
                sender.sendMessage(Message.SUCCESS_SKIN_CLEAR);
            } else {
                sender.sendMessage(Message.SUCCESS_SKIN_CLEAR_OTHER, playerName);
            }
        });
    }

    @Subcommand("search")
    @CommandPermission("%skinSearch")
    @Description("%helpSkinSearch")
    @Syntax("%SyntaxSkinSearch")
    @Conditions("cooldown")
    private void onSkinSearch(SRCommandSender sender, String searchString) {
        sender.sendMessage(Message.SKIN_SEARCH_MESSAGE, searchString);
    }

    @Subcommand("update")
    @CommandPermission("%skinUpdate")
    @Description("%helpSkinUpdate")
    @Conditions("cooldown")
    private void onSkinUpdate(SRPlayer player) {
        onSkinUpdateOther(player, new OnlineSRPlayer(player));
    }

    @Subcommand("update")
    @CommandPermission("%skinUpdateOther")
    @CommandCompletion("@players")
    @Description("%helpSkinUpdateOther")
    @Syntax("%SyntaxSkinUpdateOther")
    @Conditions("cooldown")
    private void onSkinUpdateOther(SRCommandSender sender, OnlineSRPlayer target) {
        SRPlayer targetPlayer = target.getPlayer();

        adapter.runAsync(() -> {
            String playerName = targetPlayer.getName();
            Optional<String> skin = skinStorage.getSkinNameOfPlayer(playerName);

            try {
                if (skin.isPresent()) {
                    // Filter skinUrl
                    if (skin.get().startsWith(" ")) {
                        sender.sendMessage(Message.ERROR_UPDATING_URL);
                        return;
                    }

                    if (!skinStorage.updateSkinData(skin.get())) {
                        sender.sendMessage(Message.ERROR_UPDATING_SKIN);
                        return;
                    }
                } else {
                    // get DefaultSkin
                    skin = Optional.of(skinStorage.getDefaultSkinName(playerName, true).getLeft());
                }
            } catch (SkinRequestException e) {
                sender.sendMessage(getRootCause(e).getMessage());
                return;
            }

            if (setSkin(sender, targetPlayer, skin.get(), false, null)) {
                if (sender.getName().equals(targetPlayer.getName()))
                    sender.sendMessage(Message.SUCCESS_UPDATING_SKIN);
                else
                    sender.sendMessage(Message.SUCCESS_UPDATING_SKIN_OTHER, playerName);
            }
        });
    }

    @Subcommand("set")
    @CommandPermission("%skinSet")
    @CommandCompletion("@skin")
    @Description("%helpSkinSet")
    @Syntax("%SyntaxSkinSet")
    @Conditions("cooldown")
    private void onSkinSet(SRPlayer player, String[] skin) {
        if (skin.length == 0) {
            throw new InvalidCommandArgument(true);
        }

        onSkinSetOther(player, new OnlineSRPlayer(player), skin[0], null);
    }

    @Subcommand("set")
    @CommandPermission("%skinSetOther")
    @CommandCompletion("@players @skin")
    @Description("%helpSkinSetOther")
    @Syntax("%SyntaxSkinSetOther")
    @Conditions("cooldown")
    private void onSkinSetOther(SRCommandSender sender, OnlineSRPlayer target, String skin, SkinVariant skinVariant) {
        SRPlayer targetPlayer = target.getPlayer();
        adapter.runAsync(() -> {
            if (settings.getProperty(CommandConfig.PER_SKIN_PERMISSIONS) && !sender.hasPermission("skinsrestorer.skin." + skin)) {
                if (!sender.hasPermission("skinsrestorer.ownskin") && (!sender.equalsPlayer(targetPlayer) || !skin.equalsIgnoreCase(sender.getName()))) {
                    sender.sendMessage(Message.PLAYER_HAS_NO_PERMISSION_SKIN);
                    return;
                }
            }

            if (setSkin(sender, targetPlayer, skin, true, skinVariant) && !sender.equalsPlayer(targetPlayer)) {
                sender.sendMessage(Message.SUCCESS_SKIN_CHANGE_OTHER, targetPlayer.getName());
            }
        });
    }

    @Subcommand("url")
    @CommandPermission("%skinSetUrl")
    @CommandCompletion("@skinUrl")
    @Description("%helpSkinSetUrl")
    @Syntax("%SyntaxSkinUrl")
    @Conditions("cooldown")
    private void onSkinSetUrl(SRPlayer player, String url, SkinVariant skinVariant) {
        if (!C.validUrl(url)) {
            player.sendMessage(Message.ERROR_INVALID_URLSKIN);
            return;
        }

        onSkinSetOther(player, new OnlineSRPlayer(player), url, skinVariant);
    }

    private void sendHelp(SRCommandSender sender) {
        String srLine = locale.getMessage(sender, Message.SR_LINE);
        if (!srLine.isEmpty()) {
            sender.sendMessage(srLine);
        }

        sender.sendMessage(Message.CUSTOM_HELP_IF_ENABLED, plugin.getVersion());

        if (!srLine.isEmpty()) {
            sender.sendMessage(srLine);
        }
    }

    private boolean setSkin(SRCommandSender sender, SRPlayer player, String skinName, boolean saveSkin, SkinVariant skinVariant) {
        // Escape "null" skin, this did cause crash in the past for some waterfall instances
        // TODO: resolve this in a different way
        if (skinName.equalsIgnoreCase("null")) {
            sender.sendMessage(Message.INVALID_PLAYER, skinName);
            return false;
        }

        if (settings.getProperty(CommandConfig.DISABLED_SKINS_ENABLED) && !sender.hasPermission("skinsrestorer.bypassdisabled")
                && settings.getProperty(CommandConfig.DISABLED_SKINS).stream().anyMatch(skinName::equalsIgnoreCase)) {
            sender.sendMessage(Message.ERROR_SKIN_DISABLED);
            return false;
        }

        String playerName = player.getName();
        String oldSkinName = saveSkin ? skinStorage.getSkinNameOfPlayer(playerName).orElse(playerName) : null;
        if (C.validUrl(skinName)) {
            if (!sender.hasPermission("skinsrestorer.command.set.url")
                    && !settings.getProperty(CommandConfig.SKIN_WITHOUT_PERM)) { // Ignore /skin clear when defaultSkin = url
                sender.sendMessage(Message.PLAYER_HAS_NO_PERMISSION_URL);
                return false;
            }

            if (!allowedSkinUrl(skinName)) {
                sender.sendMessage(Message.ERROR_SKINURL_DISALLOWED);
                return false;
            }

            // Apply cooldown to sender
            setCoolDown(sender, CommandConfig.SKIN_CHANGE_COOLDOWN);

            try {
                sender.sendMessage(Message.MS_UPDATING_SKIN);
                String skinUrlName = " " + playerName; // so won't overwrite premium player names
                if (skinUrlName.length() > 16) // max len of 16 char
                    skinUrlName = skinUrlName.substring(0, 16);

                SkinProperty generatedSkin = mineSkinAPI.genSkin(skinName, skinVariant);
                skinStorage.setSkinData(skinUrlName, generatedSkin, 0); // "generate" and save skin forever
                skinStorage.setSkinOfPlayer(playerName, skinUrlName); // set player to "whitespaced" name then reload skin
                skinApplier.applySkin(player.getAs(Object.class), generatedSkin);

                String success = locale.getMessage(player, Message.SUCCESS_SKIN_CHANGE);
                if (!success.isEmpty() && !success.equals(locale.getMessage(player, Message.PREFIX))) {
                    player.sendMessage(Message.SUCCESS_SKIN_CHANGE, "skinUrl");
                }

                return true;
            } catch (SkinRequestException e) {
                sender.sendMessage(getRootCause(e).getMessage());
            } catch (Exception e) {
                logger.debug(SRLogLevel.SEVERE, String.format("Could not generate skin url: %s", skinName), e);
                sender.sendMessage(Message.ERROR_INVALID_URLSKIN);
            }
        } else {
            // If skin is not an url, it's a username
            // Apply cooldown to sender
            setCoolDown(sender, CommandConfig.SKIN_CHANGE_COOLDOWN);

            try {
                if (saveSkin) {
                    skinStorage.setSkinOfPlayer(playerName, skinName);
                }

                skinApplier.applySkin(player.getAs(Object.class), skinName);

                String success = locale.getMessage(player, Message.SUCCESS_SKIN_CHANGE);
                if (!success.isEmpty() && !success.equals(locale.getMessage(player, Message.PREFIX))) {
                    player.sendMessage(Message.SUCCESS_SKIN_CHANGE, skinName); // TODO: should this not be sender? -> hidden skin set?
                }

                return true;
            } catch (SkinRequestException | NotPremiumException e) {
                sender.sendMessage(getRootCause(e).getMessage());
            }
        }

        // set CoolDown to ERROR_COOLDOWN and rollback to old skin on exception
        setCoolDown(sender, CommandConfig.SKIN_ERROR_COOLDOWN);
        if (saveSkin) {
            skinStorage.setSkinOfPlayer(playerName, oldSkinName);
        }
        return false;
    }

    private void setCoolDown(SRCommandSender sender, Property<Integer> time) {
        if (sender instanceof SRPlayer) {
            UUID senderUUID = ((SRPlayer) sender).getUniqueId();
            cooldownStorage.setCooldown(senderUUID, settings.getProperty(time), TimeUnit.SECONDS);
        }
    }

    private boolean allowedSkinUrl(String url) {
        if (settings.getProperty(CommandConfig.RESTRICT_SKIN_URLS_ENABLED)) {
            for (String possiblyAllowedUrl : settings.getProperty(CommandConfig.RESTRICT_SKIN_URLS_LIST)) {
                if (url.startsWith(possiblyAllowedUrl)) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }
}