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
package org.apache.maven.plugins.site.render;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.i18n.I18N;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Renders a sitemap report.
 *
 * @author ltheussl
 *
 * @since 2.1
 */
public class SitemapDocumentRenderer implements SitePluginReportDocumentRenderer {
    private DocumentRenderingContext docRenderingContext;

    private final String reportMojoInfo;

    String title;

    private SiteModel siteModel;

    private I18N i18n;

    private final Log log;

    public SitemapDocumentRenderer(
            MojoExecution mojoExecution,
            DocumentRenderingContext docRenderingContext,
            String title,
            SiteModel siteModel,
            I18N i18n,
            Log log) {
        this.docRenderingContext = docRenderingContext;
        this.reportMojoInfo = mojoExecution.getPlugin().getArtifactId()
                + ':'
                + mojoExecution.getPlugin().getVersion()
                + ':'
                + mojoExecution.getGoal();
        this.title = title;
        this.siteModel = siteModel;
        this.i18n = i18n;
        this.log = log;
    }

    public void renderDocument(Writer writer, SiteRenderer siteRenderer, SiteRenderingContext siteRenderingContext)
            throws RendererException, IOException {
        Locale locale = siteRenderingContext.getLocale();

        String msg = "Generating \"" + buffer().strong(title) + "\" report";
        // CHECKSTYLE_OFF: MagicNumber
        log.info((StringUtils.rightPad(msg, 40) + buffer().strong(" --- ").mojo(reportMojoInfo)));
        // CHECKSTYLE_ON: MagicNumber

        SiteRendererSink sink = new SiteRendererSink(docRenderingContext);

        sink.head();

        sink.title();

        sink.text(title);

        sink.title_();

        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text(i18n.getString("site-plugin", locale, "site.sitemap.section.title"));
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text(i18n.getString("site-plugin", locale, "site.sitemap.description"));
        sink.paragraph_();

        for (Menu menu : siteModel.getMenus()) {
            sink.section2();
            sink.sectionTitle2();
            sink.text(menu.getName());
            sink.sectionTitle2_();
            sink.horizontalRule();

            extractItems(menu.getItems(), sink);

            sink.section2_();
        }

        sink.section1_();

        sink.body_();

        sink.flush();

        sink.close();

        siteRenderer.mergeDocumentIntoSite(writer, sink, siteRenderingContext);
    }

    private static void extractItems(List<MenuItem> items, Sink sink) {
        if (items == null || items.isEmpty()) {
            return;
        }

        sink.list();

        for (MenuItem item : items) {
            sink.listItem();
            if (item.getHref() != null) {
                sink.link(relativePath(item.getHref()));
            }
            sink.text(item.getName());
            if (item.getHref() != null) {
                sink.link_();
            }

            extractItems(item.getItems(), sink);
            sink.listItem_();
        }

        sink.list_();
    }

    // sitemap.html gets generated into top-level so we only have to check leading slashes
    private static String relativePath(String href) {
        return href.startsWith("/") ? "." + href : href;
    }

    public String getOutputName() {
        return docRenderingContext.getOutputName();
    }

    public DocumentRenderingContext getRenderingContext() {
        return docRenderingContext;
    }

    public boolean isOverwrite() {
        return true;
    }

    public boolean isExternalReport() {
        return false;
    }

    @Override
    public String getReportMojoInfo() {
        return reportMojoInfo;
    }
}
