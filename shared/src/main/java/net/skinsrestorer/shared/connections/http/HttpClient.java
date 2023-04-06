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
package net.skinsrestorer.shared.connections.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RequiredArgsConstructor
public class HttpClient {
    private final String url;
    private final RequestBody requestBody;
    private final HttpType accepts;
    private final String userAgent;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final int timeout;

    public HttpResponse execute() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method.name());
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        connection.setRequestProperty("Accept", accepts.getContentType());
        connection.setRequestProperty("User-Agent", userAgent);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        connection.setDoOutput(requestBody != null);
        if (requestBody != null) {
            connection.setRequestProperty("Content-Length", String.valueOf(requestBody.getBody().length()));
            connection.setRequestProperty("Content-Type", requestBody.getType().getContentType());
            try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())) {
                output.writeBytes(requestBody.getBody());
                output.flush();
            }
        }

        connection.connect();

        StringBuilder body = new StringBuilder();
        InputStream is;
        try {
            is = connection.getInputStream();
        } catch (Exception e) {
            is = connection.getErrorStream();
        }

        try (DataInputStream input = new DataInputStream(is)) {
            for (int c = input.read(); c != -1; c = input.read()) {
                body.append((char) c);
            }
        }

        return new HttpResponse(connection.getResponseCode(), body.toString(), connection.getHeaderFields());
    }

    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE
    }

    @RequiredArgsConstructor
    public enum HttpType {
        JSON("application/json"),
        TEXT("text/plain"),
        FORM("application/x-www-form-urlencoded");

        @Getter
        private final String contentType;
    }

    @Getter
    @RequiredArgsConstructor
    public static class RequestBody {
        private final String body;
        private final HttpType type;
    }
}