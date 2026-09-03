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

import javax.inject.Named;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.resource.Resource;

/**
 * Uploading half of a <code>dav:</code> {@link Wagon}, enough for the site deployment tests to talk to
 * {@link SimpleDavServerHandler}: it PUTs every file of the site directory and, when the mojo hands it a
 * {@link org.apache.maven.wagon.proxy.ProxyInfo}, routes the request through that proxy so the tests can
 * assert on the proxy headers the server receives.
 */
@Named("dav")
public class SimpleDavWagon extends AbstractWagon {

    @Override
    public boolean supportsDirectoryCopy() {
        return true;
    }

    @Override
    public void putDirectory(File sourceDirectory, String destinationDirectory)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        for (File file : listFiles(sourceDirectory)) {
            if (file.isDirectory()) {
                putDirectory(file, destinationDirectory + "/" + file.getName());
            } else {
                put(file, destinationDirectory + "/" + file.getName());
            }
        }
    }

    @Override
    public void put(File source, String destination)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        Resource resource = new Resource(destination);
        firePutInitiated(resource, source);
        resource.setContentLength(source.length());
        resource.setLastModified(source.lastModified());
        firePutStarted(resource, source);

        try {
            HttpURLConnection connection = openConnection(destination);
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(source.length());

            try (InputStream in = Files.newInputStream(source.toPath());
                    OutputStream out = connection.getOutputStream()) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                for (int read = in.read(buffer); read >= 0; read = in.read(buffer)) {
                    out.write(buffer, 0, read);
                }
            }

            int status = connection.getResponseCode();
            connection.disconnect();

            if (status == HttpURLConnection.HTTP_UNAUTHORIZED
                    || status == HttpURLConnection.HTTP_PROXY_AUTH
                    || status == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new AuthorizationException("Not authorized to PUT " + destination + ": " + status);
            }
            if (status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new TransferFailedException("PUT of " + destination + " failed with status " + status);
            }
        } catch (IOException e) {
            throw new TransferFailedException("Error transferring " + source + " to " + destination, e);
        }

        firePutCompleted(resource, source);
    }

    private HttpURLConnection openConnection(String destination) throws IOException {
        URL url = new URL(resourceUrl(destination));
        ProxyInfo proxy = getProxyInfo(url.getProtocol(), url.getHost());

        HttpURLConnection connection;
        if (proxy == null) {
            connection = (HttpURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection(
                    new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort())));
            if (proxy.getUserName() != null) {
                connection.setRequestProperty("Proxy-Authorization", basic(proxy.getUserName(), proxy.getPassword()));
            }
        }

        AuthenticationInfo authentication = getAuthenticationInfo();
        if (authentication != null && authentication.getUserName() != null) {
            connection.setRequestProperty(
                    "Authorization", basic(authentication.getUserName(), authentication.getPassword()));
        }

        return connection;
    }

    /**
     * @param destination the resource name relative to the repository
     * @return the transport URL of the resource, that is the repository URL without its <code>dav:</code> hint
     */
    private String resourceUrl(String destination) {
        String base = getRepository().getUrl().substring("dav:".length());
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base + (destination.startsWith("/") ? destination.substring(1) : destination);
    }

    private static String basic(String userName, String password) {
        String credentials = userName + ":" + (password == null ? "" : password);
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static File[] listFiles(File directory) throws ResourceDoesNotExistException {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new ResourceDoesNotExistException("Not a directory: " + directory);
        }
        return files;
    }

    @Override
    public void get(String resourceName, File destination) {
        throw new UnsupportedOperationException("Downloading is not supported by this test wagon");
    }

    @Override
    public boolean getIfNewer(String resourceName, File destination, long timestamp) {
        throw new UnsupportedOperationException("Downloading is not supported by this test wagon");
    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
        // nothing to open, every PUT uses its own connection
    }

    @Override
    protected void closeConnection() throws ConnectionException {
        // nothing to close
    }
}
