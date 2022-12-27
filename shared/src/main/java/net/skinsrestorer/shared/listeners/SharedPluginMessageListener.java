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
package net.skinsrestorer.shared.listeners;

import lombok.RequiredArgsConstructor;
import net.skinsrestorer.shared.interfaces.ISRProxyPlayer;
import net.skinsrestorer.shared.storage.SkinStorage;
import net.skinsrestorer.shared.utils.log.SRLogger;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

@RequiredArgsConstructor
public abstract class SharedPluginMessageListener {
    private final SRLogger logger;
    private final SkinStorage skinStorage;
    private final Function<String, Optional<ISRProxyPlayer>> playerGetter;

    private static byte[] convertToByteArray(Map<String, String> map) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

        try {
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
                ObjectOutputStream out = new ObjectOutputStream(gzipOut);
                out.writeObject(map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteOut.toByteArray();
    }

    public void sendPage(int page, ISRProxyPlayer player) {
        int skinNumber = 36 * page;

        byte[] ba = convertToByteArray(skinStorage.getSkins(skinNumber));

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("returnSkinsV2");
            out.writeUTF(player.getName());
            out.writeInt(page);

            out.writeShort(ba.length);
            out.write(ba);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        byte[] data = b.toByteArray();
        logger.debug(String.format("Sending skins to %s (%d bytes)", player.getName(), data.length));
        // Payload may not be larger than 32767 bytes -18 from channel name
        if (data.length > 32749) {
            logger.warning("Too many bytes GUI... canceling GUI..");
            return;
        }

        player.sendDataToServer("sr:messagechannel", data);
    }

    public void handlePluginMessage(SRPluginMessageEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getTag().equals("sr:messagechannel") && !event.getTag().equals("sr:skinchange"))
            return;

        if (!event.isServerConnection()) {
            event.setCancelled(true);
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        try {
            String subChannel = in.readUTF();
            Optional<ISRProxyPlayer> optional = playerGetter.apply(in.readUTF());

            if (!optional.isPresent())
                return;

            ISRProxyPlayer player = optional.get();

            switch (subChannel) {
                // sr:messagechannel
                case "getSkins":
                    int page = Math.min(in.readInt(), 999);
                    sendPage(page, player);
                    break;
                case "clearSkin":
                    player.forceExecuteCommand("skin clear");
                    break;
                case "updateSkin":
                    player.forceExecuteCommand("skin update");
                    break;
                case "setSkin":
                    String skin = in.readUTF();
                    player.forceExecuteCommand("skin set " + skin);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
