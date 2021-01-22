package net.skinsrestorer.shared.storage;

import net.skinsrestorer.shared.interfaces.ISkinStorage;
import net.skinsrestorer.shared.storage.backend.SkinStorageFlatFile;
import net.skinsrestorer.shared.storage.backend.SkinStorageMySQL;

import java.util.Map;

/**
 * Created by McLive on 10.11.2019.
 */
public class SkinStorageHandler implements ISkinStorage {
    private SkinStorageFlatFile skinStorageFlatFile;
    private SkinStorageMySQL skinStorageMySQL;

    @Override
    public String getPlayerSkin(String player) {
        if (Config.USE_MYSQL)
            return skinStorageMySQL.getPlayerSkin(player);

        return skinStorageFlatFile.getPlayerSkin(player);
    }

    @Override
    public Object getSkinData(String name, boolean updateOutdated) {
        if (Config.USE_MYSQL)
            return skinStorageMySQL.getSkinData(name, updateOutdated);

        return skinStorageFlatFile.getSkinData(name, updateOutdated);
    }

    @Override
    public Object getSkinData(String name) {
        return this.getSkinData(name, true);
    }

    @Override
    public void removePlayerSkin(String name) {
        if (Config.USE_MYSQL)
            skinStorageMySQL.removePlayerSkin(name);
        else
            skinStorageFlatFile.removePlayerSkin(name);
    }

    @Override
    public void setPlayerSkin(String name, String skin) {
        if (Config.USE_MYSQL)
            skinStorageMySQL.setPlayerSkin(name, skin);
        else
            skinStorageFlatFile.setPlayerSkin(name, skin);
    }

    @Override
    public Map<String, Object> getSkins(int number) {
        if (Config.USE_MYSQL)
            return skinStorageMySQL.getSkins(number);

        return skinStorageFlatFile.getSkins(number);
    }

    @Override
    public String getDefaultSkinNameIfEnabled(String player, boolean clear) {
        if (Config.USE_MYSQL)
            return skinStorageMySQL.getDefaultSkinNameIfEnabled(player, clear);

        return skinStorageFlatFile.getDefaultSkinNameIfEnabled(player, clear);
    }

    @Override
    public String getDefaultSkinNameIfEnabled(String player) {
        return this.getDefaultSkinNameIfEnabled(player, false);
    }
}
