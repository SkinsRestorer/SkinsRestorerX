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
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.shared.interfaces.SRPlayer;
import net.skinsrestorer.shared.interfaces.SRProxyPlayer;
import net.skinsrestorer.shared.platform.SRProxyPlugin;
import net.skinsrestorer.shared.storage.Message;
import net.skinsrestorer.shared.storage.SkinStorageImpl;
import net.skinsrestorer.shared.utils.log.SRLogger;

import javax.inject.Inject;

@SuppressWarnings("unused")
@CommandAlias("skins")
@CommandPermission("%skins")
@Conditions("cooldown|allowed-server")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class ProxyGUICommand extends BaseCommand {
    private final SRLogger logger;
    private final SkinStorageImpl skinStorage;

    @Default
    private void onDefault(SRPlayer player) {
        if (!(player instanceof SRProxyPlayer)) {
            throw new IllegalStateException("Player is not a proxy player");
        }

        player.sendMessage(Message.SKINSMENU_OPEN);

        SRProxyPlugin.sendPage(0, (SRProxyPlayer) player, logger, skinStorage);
    }
}