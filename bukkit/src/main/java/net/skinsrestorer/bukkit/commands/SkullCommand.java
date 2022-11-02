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
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.SkinVariant;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.SkullSource;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.property.IProperty;
import net.skinsrestorer.bukkit.SkinSkull;
import net.skinsrestorer.bukkit.SkinsRestorer;
import net.skinsrestorer.shared.interfaces.ISRPlayer;
import net.skinsrestorer.shared.storage.CooldownStorage;
import net.skinsrestorer.shared.storage.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static net.skinsrestorer.bukkit.utils.WrapperBukkit.wrapPlayer;

@RequiredArgsConstructor
@CommandAlias("Skull")
@CommandPermission("%skull")
public class SkullCommand extends BaseCommand {
    private final SkinsRestorer plugin;
    private final SkinSkull SkinsSkull;

    @HelpCommand
    @Syntax("%helpHelpCommand")
    public static void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Default
    @CommandPermission("%skullGet")
    public void onDefault(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ISRPlayer srPlayer = wrapPlayer(player);

            CooldownStorage cooldownStorage = plugin.getCooldownStorage();
            if (!player.hasPermission("skinsrestorer.bypasscooldown") && cooldownStorage.hasCooldown(player.getName())) {
                srPlayer.sendMessage(Message.SKIN_COOLDOWN, cooldownStorage.getCooldownSeconds(player.getName()));
                return;
            }

            Optional<String> skinName = plugin.getSkinStorage().getSkinNameOfPlayer(srPlayer.getName());
            if (skinName.isPresent()) {
                Optional<IProperty> skinData = plugin.getSkinStorage().getSkinData(skinName.get(), false);

                if (skinData.isPresent()) {
                    srPlayer.sendMessage("Here you go, your skull!");
                    SkinSkull.giveSkull(plugin, srPlayer.getName(), player, skinData.get().getValue());
                } else {
                    srPlayer.sendMessage(Message.NO_SKIN_DATA);
                }
            }
        });
    }

    @Subcommand("get")
    @CommandPermission("%skullGet")
    @Description("%helpSkullGet")
    @Syntax("%SyntaxSkullGet")
    public void onGet(CommandSender sender, SkullSource skullsource, String value, @co.aikar.commands.annotation.Optional SkinVariant[] skinVariant) {
        giveSkull((Player) sender, (Player) sender, sender.getName(), skullsource, value, skinVariant);
    }

    @Subcommand("give")
    @CommandPermission("%skullGive")
    @CommandCompletion("@players")
    @Description("%helpSkullGive")
    @Syntax("%SyntaxSkullGive")
    public void onGive(CommandSender sender, Player player, SkullSource skullsource, String value, @co.aikar.commands.annotation.Optional SkinVariant[] skinVariant) {
        giveSkull((Player) sender, player, player.getName(), skullsource, value, skinVariant);
    }

    @Subcommand("update")
    @CommandPermission("%skullUpdate")
    @Description("%helpSkullUpdate")
    public void onUpdate(CommandSender sender) {
        //todo Async

        // todo: add seperate cooldown storage
        CooldownStorage cooldownStorage = plugin.getCooldownStorage();
        if (!sender.hasPermission("skinsrestorer.bypasscooldown") && cooldownStorage.hasCooldown(sender.getName())) {
            sender.sendMessage(String.valueOf(Message.SKIN_COOLDOWN), String.valueOf(cooldownStorage.getCooldownSeconds(sender.getName())));
            return;
        }

        //skullupdate(sender);
    }

    @Subcommand("props")
    @CommandPermission("%skullProps")
    @Description("%helpSkullProps")
    public void onProps(CommandSender sender) {
        //todo Async

    }

    boolean giveSkull(Player sender, Player targetPlayer, String skullOwner, SkullSource skullsource, String value, SkinVariant[] skinVariant) {
        //todo async

        // todo: add seperate cooldown storage
        CooldownStorage cooldownStorage = plugin.getCooldownStorage();
        if (!sender.hasPermission("skinsrestorer.bypasscooldown") && cooldownStorage.hasCooldown(sender.getName())) {
            sender.sendMessage(String.valueOf(Message.SKIN_COOLDOWN), String.valueOf(cooldownStorage.getCooldownSeconds(sender.getName())));
            return false;
        }

        // perms
        if ((skullsource == SkullSource.PLAYER || skullsource == SkullSource.MOJANGPLAYER || skullsource == SkullSource.SKIN) && !sender.hasPermission("skinsrestorer.skull.get.other") || value.equals(sender.getName())) {
            sender.sendMessage("no perms to get custom skull!"); //TODO: custom NoPerms message
            return false;
        }

        if ((skullsource == SkullSource.SKINURL || skullsource == SkullSource.TEXTUREVALUE) && !sender.hasPermission("skinsrestorer.skull.get.url")) {
            sender.sendMessage("no perms to get custom skull!"); //TODO: custom NoPerms message
            return false;
        }

        // converting skull source to base64 skin value
        String base64value;
        switch (skullsource) {
            case MOJANGPLAYER:
                base64value = SkinsRestorerAPI.getApi().getProfile(value).getValue();
                break;
            case PLAYER:
                base64value = SkinsRestorerAPI.getApi().getSkinData(SkinsRestorerAPI.getApi().getSkinName(value)).getValue();
                break;
            case SKIN:
                base64value = SkinsRestorerAPI.getApi().getSkinData(value).getValue();
                break;
            case SKINURL:
                try {
                    base64value = SkinsRestorerAPI.getApi().genSkinUrl(value, SkinVariant.valueOf(String.valueOf(skinVariant))).getValue(); //todo: exception handling
                } catch (SkinRequestException e) {
                    sender.sendMessage(e.getCause().getMessage());
                    return false;
                }
                break;
            case TEXTUREVALUE:
                base64value = value;
                break;
            default:
                sender.sendMessage("Invalid skull source");
                return false;
        }
        SkinSkull.giveSkull(plugin, targetPlayer.getName(), targetPlayer, base64value);
        return true;
    }

}