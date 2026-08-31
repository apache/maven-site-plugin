/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.site.deploy;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Dummy WebDAV server for tests: records every request, stores PUT bodies as files
 * and optionally requires Basic proxy authentication before doing so.
 *
 * @author <a href="mailto:olamy@apache.org">Olivier Lamy</a>
 * @since 3.0-beta-2
 */
public class SimpleDavServerHandler {

    private final HttpServer server;

    private final File siteTargetPath;

    private final Map<String, String> authentications;

    List<HttpRequest> httpRequests = new ArrayList<>();

    public SimpleDavServerHandler(final File targetPath) throws IOException {
        this(targetPath, null);
    }

    public SimpleDavServerHandler(final File targetPath, Map<String, String> authentications) throws IOException {
        this.siteTargetPath = targetPath;
        this.authentications = authentications;
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", this::handleRequest);
        server.start();
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        if (authentications != null && !authentications.isEmpty() && !isAuthorized(exchange)) {
            exchange.getResponseHeaders()
                    .add("Proxy-Authenticate", "Basic realm=\"Maven Site Plugin Proxy Authorization\"");
            exchange.sendResponseHeaders(407, -1);
            return;
        }

        String targetPath = exchange.getRequestURI().getPath();

        HttpRequest rq = new HttpRequest();
        rq.method = exchange.getRequestMethod();
        rq.path = targetPath;

        for (Map.Entry<String, List<String>> entry :
                exchange.getRequestHeaders().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                rq.headers.put(
                        entry.getKey().toLowerCase(Locale.ROOT),
                        entry.getValue().get(0));
            }
        }

        httpRequests.add(rq);

        if ("PUT".equalsIgnoreCase(rq.method)) {
            File targetFile = new File(siteTargetPath, targetPath);
            targetFile.getParentFile().mkdirs();
            Files.copy(exchange.getRequestBody(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        exchange.sendResponseHeaders(200, -1);
    }

    private boolean isAuthorized(HttpExchange exchange) {
        String proxyAuthorization = exchange.getRequestHeaders().getFirst("Proxy-Authorization");
        if (proxyAuthorization == null || !proxyAuthorization.startsWith("Basic ")) {
            return false;
        }

        String authorization = new String(Base64.getDecoder().decode(proxyAuthorization.substring(6)));
        String[] authTokens = authorization.split(":");
        String user = authTokens[0];
        String password = authTokens[1];

        if (authentications.get(user) == null) {
            throw new IllegalArgumentException(user + " not found in the map!");
        }

        return password.equals(authentications.get(user));
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public void stop() {
        server.stop(0);
    }
}
