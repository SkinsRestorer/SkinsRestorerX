/*
 * SkinsRestorer
 *
 * Copyright (C) 2023 SkinsRestorer
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
package net.skinsrestorer.api.storage;

import net.skinsrestorer.api.connections.model.MineSkinResponse;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinIdentifier;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.property.SkinVariant;

import java.util.Optional;
import java.util.UUID;

/**
 * SkinStorage
 * <br/>
 * There are three types of skins:
 * - Player skins
 * - URL skins
 * - Custom skins
 * <br/>
 * URL skins additionally store an "index"; an index is the skin variant that was generated by MineSkin.
 * That means when you type an url, we'll know whether to automatically choose the slim or classic variant.
 * The user has the option to override the default "index variant" using the command /skin set url variant
 * <br/>
 * A player-skin is stored using the target player's UUID.
 * This way we can, for example, keep using the skin of "Dinnerbone" even if
 * he changes his name to "Spoiledbone".
 * A player-skin may be fetched again if the skin data is outdated. (By timestamp)
 * <br/>
 * A custom skin is stored using the custom skin name. It will never be updated automatically.
 */
public interface SkinStorage {
    /**
     * This method returns the skin data associated to the skin name.
     * If the skin name is not found, it will try to get the skin data from Mojang.
     *
     * @param uuid Player UUID
     * @return The skin property containing the skin data
     * @throws DataRequestException If MojangAPI lookup errors (e.g. premium player not found)
     */
    Optional<SkinProperty> updatePlayerSkinData(UUID uuid) throws DataRequestException;

    /**
     * Saves a player skin to the database.
     *
     * @param uuid      Player UUID
     * @param textures  Property object
     * @param timestamp timestamp string in milliseconds
     */
    void setPlayerSkinData(UUID uuid, SkinProperty textures, long timestamp);

    /**
     * Saves an url skin to the database.
     *
     * @param url         URL to skin
     * @param mineSkinId  MineSkin ID
     * @param textures    Property object
     * @param skinVariant Skin variant
     */
    void setURLSkinData(String url, String mineSkinId, SkinProperty textures, SkinVariant skinVariant);

    /**
     * Saves an url index to the database.
     *
     * @param url         URL to skin
     * @param skinVariant Skin variant
     */
    void setURLSkinIndex(String url, SkinVariant skinVariant);

    /**
     * Saves an url to the database using a MineSkinResponse object.
     *
     * @param url      URL to skin
     * @param response MineSkinResponse object
     */
    default void setURLSkinByResponse(String url, MineSkinResponse response) {
        if (response.getRequestedVariant() == null) {
            setURLSkinIndex(url, response.getGeneratedVariant());
        }

        setURLSkinData(url, response.getMineSkinId(), response.getProperty(), response.getGeneratedVariant());
    }

    /**
     * Saves a custom skin to the database.
     *
     * @param skinName Skin name
     * @param textures Property object
     */
    void setCustomSkinData(String skinName, SkinProperty textures);

    /**
     * Searches a skin in the database by name/url.
     *
     * @param input Skin name/url
     * @return InputDataResult object or empty if not found
     */
    Optional<InputDataResult> findSkinData(String input);

    /**
     * Searches a skin in the database by name/url. If not found, it will try to generate it based on the detected type.
     *
     * @param input Skin name/url
     * @return InputDataResult object or empty if not found
     * @throws DataRequestException If MojangAPI lookup errors (e.g. premium player not found)
     */
    Optional<InputDataResult> findOrCreateSkinData(String input) throws DataRequestException;

    /**
     * Searches a skin in the database by its skin identifier.
     *
     * @param identifier Skin identifier
     * @return SkinProperty object or empty if not found
     */
    Optional<SkinProperty> getSkinDataByIdentifier(SkinIdentifier identifier);

    /**
     * Removes a skin from the database by its skin identifier.
     *
     * @param identifier Skin identifier
     */
    void removeSkinData(SkinIdentifier identifier);
}
