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
package org.apache.maven.plugins.site.run;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.DoxiaDocumentRenderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.site.render.ReportDocumentRenderer;
import org.apache.maven.plugins.site.render.SitePluginReportDocumentRenderer;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Render a page as requested.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DoxiaHandler implements HttpHandler {
    private final Path outputDirectory;

    private final SiteRenderer siteRenderer;

    private final Map<String, DoxiaBean> i18nDoxiaContexts;

    private final List<Locale> localesList;

    private final Log log;

    public DoxiaHandler(
            File outputDirectory,
            SiteRenderer siteRenderer,
            Map<String, DoxiaBean> i18nDoxiaContexts,
            List<Locale> localesList,
            Log log) {
        this.outputDirectory = outputDirectory.toPath().toAbsolutePath().normalize();
        this.siteRenderer = siteRenderer;
        this.i18nDoxiaContexts = i18nDoxiaContexts;
        this.localesList = localesList;
        this.log = log;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            // welcome file
            if (path.endsWith("/")) {
                path += "index.html";
            }

            // Remove the /
            path = path.substring(1);
            String fullPath = path;

            // Handle locale request
            String localeWanted = "";
            for (Locale locale : localesList) {
                if (path.startsWith(locale + "/")) {
                    localeWanted = locale.toString();
                    path = path.substring(localeWanted.length() + 1);
                }
            }

            DoxiaBean doxiaBean;
            if (!localeWanted.equals(SiteTool.DEFAULT_LOCALE.toString())) {
                doxiaBean = i18nDoxiaContexts.get(localeWanted);
                if (doxiaBean == null) {
                    sendError(exchange, 500, "No Doxia bean found for locale '" + localeWanted + "'");
                    return;
                }
            } else {
                doxiaBean = i18nDoxiaContexts.get("default");
                if (doxiaBean == null) {
                    sendError(exchange, 500, "No Doxia bean found for the default locale");
                    return;
                }
            }

            Map<String, DocumentRenderer> documents = doxiaBean.getDocuments();
            if (documents.containsKey(path)) {
                renderDocument(exchange, path, localeWanted, documents.get(path), doxiaBean.getContext());
            } else {
                serveStaticContent(exchange, fullPath);
            }
        } catch (RendererException e) {
            sendError(exchange, 500, "Error rendering document: " + e.getMessage());
        }
    }

    private void renderDocument(
            HttpExchange exchange,
            String path,
            String locale,
            DocumentRenderer docRenderer,
            SiteRenderingContext context)
            throws IOException, RendererException {
        logDocumentRenderer(path, locale, docRenderer);
        String outputName = docRenderer.getOutputName();

        if (docRenderer instanceof ReportDocumentRenderer
                && ((ReportDocumentRenderer) docRenderer).isExternalReport()) {
            Path externalReportFile = outputDirectory.resolve(outputName).normalize();
            if (!externalReportFile.startsWith(outputDirectory) || !Files.isRegularFile(externalReportFile)) {
                sendError(exchange, 404, "External report not found: " + outputName);
                return;
            }
            byte[] content = Files.readAllBytes(externalReportFile);
            send(exchange, 200, getMimeType(outputName, null), content);
            return;
        }

        Writer writer = new StringWriter();
        docRenderer.renderDocument(writer, siteRenderer, context);

        String encoding = context.getOutputEncoding();
        byte[] content = writer.toString().getBytes(Charset.forName(encoding));
        send(exchange, 200, getMimeType(outputName, encoding), content);
    }

    private void serveStaticContent(HttpExchange exchange, String path) throws IOException {
        Path file = outputDirectory.resolve(path).normalize();
        if (!file.startsWith(outputDirectory) || !Files.isRegularFile(file)) {
            sendError(exchange, 404, "Not found: " + path);
            return;
        }

        byte[] content = Files.readAllBytes(file);
        send(exchange, 200, getMimeType(file.getFileName().toString(), null), content);
    }

    private void send(HttpExchange exchange, int statusCode, String contentType, byte[] content) throws IOException {
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }
        exchange.sendResponseHeaders(statusCode, content.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(content);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        send(exchange, statusCode, "text/plain; charset=utf-8", message.getBytes(Charset.forName("UTF-8")));
    }

    private String getMimeType(String name, String encoding) {
        String mimeType = URLConnection.guessContentTypeFromName(name);
        if (mimeType == null) {
            return null;
        }
        if (encoding != null && mimeType.startsWith("text/")) {
            return mimeType + "; charset=" + encoding;
        }
        return mimeType;
    }

    private void logDocumentRenderer(String path, String locale, DocumentRenderer docRenderer) {
        String source;
        if (docRenderer instanceof DoxiaDocumentRenderer) {
            source = docRenderer.getRenderingContext().getDoxiaSourcePath();
        } else if (docRenderer instanceof ReportDocumentRenderer) {
            source = ((ReportDocumentRenderer) docRenderer).getReportMojoInfo();
            if (source == null) {
                source = "(unknown)";
            }
        } else if (docRenderer instanceof SitePluginReportDocumentRenderer) {
            source = ((SitePluginReportDocumentRenderer) docRenderer).getReportMojoInfo();
        } else {
            source = docRenderer.getRenderingContext().getGenerator() != null
                    ? docRenderer.getRenderingContext().getGenerator()
                    : docRenderer.getClass().getName();
        }
        String localizedPath = !locale.equals(SiteTool.DEFAULT_LOCALE.toString()) ? locale + "/" + path : path;
        String localizedSource = source
                + (!locale.equals(SiteTool.DEFAULT_LOCALE.toString())
                        ? " (locale '" + locale + "')"
                        : " (default locale)");
        log.info(localizedPath + " -> " + buffer().strong(localizedSource));
    }
}
