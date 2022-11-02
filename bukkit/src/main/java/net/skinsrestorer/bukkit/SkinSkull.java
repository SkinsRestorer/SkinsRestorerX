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
package net.skinsrestorer.bukkit;

import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.bukkit.BukkitHeadAPI;
import net.skinsrestorer.shared.utils.C;
import net.skinsrestorer.shared.utils.log.SRLogger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class SkinSkull extends ItemStack {
    private final SkinsRestorer plugin;
    private final SRLogger log;

    public static boolean giveSkull(SkinsRestorer plugin, String skullOwnerName, Player targetPlayer, @Nullable String b64stringTexture) {
        try {
            ItemStack skull = createSkull(plugin, skullOwnerName, targetPlayer, b64stringTexture);
            SkullMeta sm = (SkullMeta) Objects.requireNonNull(skull).getItemMeta();
            sm.setOwningPlayer(targetPlayer);
            skull.setItemMeta(sm);

            HashMap<Integer, ItemStack> fullInventory = targetPlayer.getInventory().addItem(skull);
            if (fullInventory.isEmpty()) {
                targetPlayer.sendMessage("Skull given"); //todo: add to lang
            } else {
                targetPlayer.sendMessage("Inventory is full, skull could not be given"); //todo: add to lang
                return true;
            }
        } catch (Exception e) {
            plugin.getSrLogger().warning("Error while giving skull to " + targetPlayer.getName() + ": " + e.getMessage());
        }
        return false;
    }

    private static ItemStack createSkull(SkinsRestorer plugin, String skullOwnerName, @Nullable Player SkullOwner, @Nullable String b64stringTexture) {
        if (SkullOwner == null && b64stringTexture == null) {
            return null;
        }

        ItemStack is = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta sm = (SkullMeta) Objects.requireNonNull(is).getItemMeta();

        // Set skull owner if player is premium
        if (SkullOwner != null && sm != null) {
            sm.setOwningPlayer(SkullOwner);
        }

        List<String> lore = new ArrayList<>();
        lore.add(C.c(" " + skullOwnerName + " Head")); // todo make customizable locale
        sm.setLore(lore);

        is.setItemMeta(sm);

        try {
            BukkitHeadAPI.setSkull(is, b64stringTexture);

        } catch (Exception e) {
            plugin.getSrLogger().warning("ERROR: could not add skin data to skull, skin might be corrupted or invalid!");
            e.printStackTrace();
        }

        return is;
    }
}