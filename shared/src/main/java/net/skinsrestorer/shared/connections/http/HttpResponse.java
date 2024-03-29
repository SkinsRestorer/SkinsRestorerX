/*
 * SkinsRestorer
 * Copyright (C) 2024  SkinsRestorer Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.skinsrestorer.shared.connections.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.shared.exception.DataRequestExceptionShared;

import java.util.List;
import java.util.Map;

public record HttpResponse(int statusCode, String body, Map<String, List<String>> headers) {
    private static final Gson GSON = new Gson();

    public <T> T getBodyAs(Class<T> clazz) throws DataRequestException {
        try {
            return GSON.fromJson(body, clazz);
        } catch (JsonSyntaxException e) {
            throw new DataRequestExceptionShared(e);
        }
    }
}
