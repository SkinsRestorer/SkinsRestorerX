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
package net.skinsrestorer.bukkit.listener;

import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.bukkit.SRBukkitAdapter;
import net.skinsrestorer.bukkit.SkinApplierBukkit;
import net.skinsrestorer.shared.listeners.SRLoginProfileEvent;
import org.bukkit.event.player.PlayerEvent;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EventWrapper {
    private final SRBukkitAdapter plugin;
    private final SkinApplierBukkit skinApplier;

    protected SRLoginProfileEvent<Void> wrap(PlayerEvent event) {
        return new SRLoginProfileEvent<Void>() {
            @Override
            public boolean isOnline() {
                return !skinApplier.getPlayerProperties(event.getPlayer()).isEmpty();
            }

            @Override
            public String getPlayerName() {
                return event.getPlayer().getName();
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void setResultProperty(SkinProperty property) {
                skinApplier.applySkin(event.getPlayer(), property);
            }

            @Override
            public Void runAsync(Runnable runnable) {
                plugin.runAsync(runnable);
                return null;
            }
        };
    }
}