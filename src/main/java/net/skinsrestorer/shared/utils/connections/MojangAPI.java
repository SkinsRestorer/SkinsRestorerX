/*
 * #%L
 * SkinsRestorer
 * %%
 * Copyright (C) 2021 SkinsRestorer
 * %%
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
 * #L%
 */
package net.skinsrestorer.shared.utils.connections;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.skinsrestorer.api.exception.SkinRequestException;
import net.skinsrestorer.api.property.*;
import net.skinsrestorer.shared.exception.ReflectionException;
import net.skinsrestorer.shared.serverinfo.Platform;
import net.skinsrestorer.shared.storage.Locale;
import net.skinsrestorer.shared.utils.MetricsCounter;
import net.skinsrestorer.shared.utils.ReflectionUtil;
import net.skinsrestorer.shared.utils.log.SRLogLevel;
import net.skinsrestorer.shared.utils.log.SRLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MojangAPI {
    private static final String UUID_URL_ASHCON = "https://api.ashcon.app/mojang/v2/user/%name%";
    private static final String UUID_URL_MOJANG = "https://api.mojang.com/users/profiles/minecraft/%name%";
    private static final String UUID_URL_MINETOOLS = "https://api.minetools.eu/uuid/%name%";

    private static final String SKIN_URL_ASHCON = "https://api.ashcon.app/mojang/v2/user/%uuid%";
    private static final String SKIN_URL_MOJANG = "https://sessionserver.mojang.com/session/minecraft/profile/%uuid%?unsigned=false";
    private static final String SKIN_URL_MINETOOLS = "https://api.minetools.eu/profile/%uuid%";

    private final SRLogger logger;
    private final Platform platform;
    private final MetricsCounter metricsCounter;
    private Class<? extends IProperty> propertyClass;

    public MojangAPI(SRLogger logger, Platform platform, MetricsCounter metricsCounter) {
        this.logger = logger;
        this.platform = platform;
        this.metricsCounter = metricsCounter;

        if (platform == Platform.BUKKIT) {
            propertyClass = BukkitProperty.class;
        } else if (platform == Platform.BUNGEECORD) {
            propertyClass = BungeeProperty.class;
        } else if (platform == Platform.VELOCITY) {
            propertyClass = VelocityProperty.class;
        }
    }

    public IProperty createProperty(String name, String value, String signature) {
        // use our own property class if we are on sponge
        if (platform == Platform.SPONGE)
            return new GenericProperty(name, value, signature);

        try {
            return (IProperty) ReflectionUtil.invokeConstructor(propertyClass, name, value, signature);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get the skin from a single request
     *
     * @param nameOrUuid name or trimmed (-) uuid
     * @return IProperty skin
     * @throws SkinRequestException on not premium or error
     */
    public IProperty getSkin(String nameOrUuid) throws SkinRequestException {
        IProperty skin = getProfile(nameOrUuid, false);
        if (skin != null)
            return skin;

        if (!nameOrUuid.matches("[a-f0-9]{32}"))
            nameOrUuid = getUUIDMojang(nameOrUuid, true);

        skin = getProfileMojang(nameOrUuid, true);

        return skin;
    }

    // TODO: Deal with duplicated code

    public String getUUID(String name) throws SkinRequestException {
        return getUUID(name, true);
    }

    /**
     * @param name Name of the player
     * @return Dash-less UUID (String)
     * @throws SkinRequestException If player is NOT_PREMIUM or server is RATE_LIMITED
     */
    protected String getUUID(String name, boolean tryNext) throws SkinRequestException {
        try {
            final String output = readURL(UUID_URL_ASHCON.replace("%name%", name), MetricsCounter.Service.ASHCON);
            final JsonObject obj = new Gson().fromJson(output, JsonObject.class);

            if (obj.has("code")) {
                if (obj.get("code").getAsInt() == 404) {
                    throw new SkinRequestException(Locale.NOT_PREMIUM);
                }
                //throw new SkinRequestException(Locale.ALT_API_FAILED); <- WIP (might not be good when there is a 202 mojang down error)
            }

            if (obj.has("uuid"))
                return obj.get("uuid").getAsString().replace("-", "");
        } catch (IOException ignored) {
        }
        if (tryNext)
            return getUUIDMojang(name, true);

        return null;
    }

    public String getUUIDMojang(String name, boolean tryNext) throws SkinRequestException {
        if (tryNext)
            logger.debug("Trying Mojang API to get UUID for player " + name + ".");

        try {
            final String output = readURL(UUID_URL_MOJANG.replace("%name%", name), MetricsCounter.Service.MOJANG);

            //todo get http code instead of checking for isEmpty
            if (output.isEmpty())
                throw new SkinRequestException(Locale.NOT_PREMIUM);

            final JsonObject obj = new Gson().fromJson(output, JsonObject.class);
            if (obj.has("error")) {
                if (tryNext)
                    return getUUIDBackup(name, true);
                return null;
            }

            return obj.get("id").getAsString();
        } catch (IOException ignored) {
        }
        if (tryNext)
            return getUUIDBackup(name, true);

        return null;
    }

    protected String getUUIDBackup(String name, boolean tryNext) throws SkinRequestException {
        if (tryNext)
            logger.debug("Trying backup API to get UUID for player " + name + ".");

        try {
            final String output = readURL(UUID_URL_MINETOOLS.replace("%name%", name), MetricsCounter.Service.MINE_TOOLS, 10000);
            final JsonObject obj = new Gson().fromJson(output, JsonObject.class);

            /* Depricated code
            if (obj.has("status") && obj.get("status").getAsString().equalsIgnoreCase("ERR")) {
                return getUUIDMojang(name, true);
            } */

            if (obj.get("id") != null)
                return obj.get("id").getAsString();
        } catch (IOException ignored) {
        }
        throw new SkinRequestException(Locale.NOT_PREMIUM); // TODO: check flow of code
    }

    public IProperty getProfile(String uuid) {
        return getProfile(uuid, true);
    }

    public IProperty getProfile(String uuid, boolean tryNext) {
        try {
            final String output = readURL(SKIN_URL_ASHCON.replace("%uuid%", uuid), MetricsCounter.Service.ASHCON);
            final JsonObject obj = new Gson().fromJson(output, JsonObject.class);

            if (obj.has("textures")) {
                final JsonObject textures = obj.get("textures").getAsJsonObject();
                final JsonObject rawTextures = textures.get("raw").getAsJsonObject();

                return createProperty("textures", rawTextures.get("value").getAsString(), rawTextures.get("signature").getAsString());
            }
        } catch (Exception ignored) {
        }
        if (tryNext)
            return getProfileMojang(uuid, true);

        return null;
    }

    public IProperty getProfileMojang(String uuid, boolean tryNext) {
        if (tryNext)
            logger.debug("Trying Mojang API to get skin property for " + uuid + ".");

        try {
            final String output = readURL(SKIN_URL_MOJANG.replace("%uuid%", uuid), MetricsCounter.Service.MOJANG);
            final JsonObject obj = new Gson().fromJson(output, JsonObject.class);
            final GenericProperty property = new GenericProperty();
            if (obj.has("properties") && property.valuesFromJson(obj)) {
                return createProperty("textures", property.getValue(), property.getSignature());
            }
        } catch (Exception ignored) {
        }
        if (tryNext)
            return getProfileBackup(uuid, true);

        return null;
    }

    protected IProperty getProfileBackup(String uuid, boolean tryNext) {
        if (tryNext)
            logger.debug("Trying backup API to get skin property for " + uuid + ".");

        try {
            final String output = readURL(SKIN_URL_MINETOOLS.replace("%uuid%", uuid), MetricsCounter.Service.MINE_TOOLS, 10000);
            final JsonObject obj = new Gson().fromJson(output, JsonObject.class);
            if (obj.has("raw")) {
                final JsonObject raw = obj.getAsJsonObject("raw");
                // Break on ERR
                if (raw.has("status") && raw.get("status").getAsString().equalsIgnoreCase("ERR")) {
                    throw new SkinRequestException("");
                }

                GenericProperty property = new GenericProperty();
                if (property.valuesFromJson(raw)) {
                    return createProperty("textures", property.getValue(), property.getSignature());
                }
            }
        } catch (Exception ignored) {
        }
        if (tryNext)
            logger.debug(SRLogLevel.WARNING, "Failed to get skin property from backup API. (" + uuid + ")");

        return null;
    }

    private String readURL(String url, MetricsCounter.Service service) throws IOException {
        return readURL(url, service, 5000);
    }

    private String readURL(String url, MetricsCounter.Service service, int timeout) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        metricsCounter.increment(service);

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "SkinsRestorer");
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setDoOutput(true);

        String line;
        StringBuilder output = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        while ((line = in.readLine()) != null)
            output.append(line);

        in.close();
        return output.toString();
    }
}
