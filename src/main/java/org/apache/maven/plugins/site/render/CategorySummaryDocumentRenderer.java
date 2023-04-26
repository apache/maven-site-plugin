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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReport;
import org.codehaus.plexus.i18n.I18N;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Renders a Maven report.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class CategorySummaryDocumentRenderer implements SitePluginReportDocumentRenderer {
    private DocumentRenderingContext docRenderingContext;

    private final String reportMojoInfo;

    private String title;

    private String desc1;

    private String desc2;

    private I18N i18n;

    private List<MavenReport> categoryReports;

    private final Log log;

    public CategorySummaryDocumentRenderer(
            MojoExecution mojoExecution,
            DocumentRenderingContext docRenderingContext,
            String title,
            String desc1,
            String desc2,
            I18N i18n,
            List<MavenReport> categoryReports,
            Log log) {
        this.docRenderingContext = docRenderingContext;
        this.reportMojoInfo = mojoExecution.getPlugin().getArtifactId()
                + ':'
                + mojoExecution.getPlugin().getVersion()
                + ':'
                + mojoExecution.getGoal();
        this.title = title;
        this.desc1 = desc1;
        this.desc2 = desc2;
        this.i18n = i18n;
        this.categoryReports = Collections.unmodifiableList(categoryReports);
        this.log = log;
    }

    public void renderDocument(Writer writer, SiteRenderer siteRenderer, SiteRenderingContext siteRenderingContext)
            throws RendererException, IOException {
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
        sink.text(title);
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text(desc1 + " ");
        sink.link("https://maven.apache.org");
        sink.text("Maven");
        sink.link_();
        sink.text(" " + desc2);
        sink.paragraph_();

        sink.section2();
        sink.sectionTitle2();
        Locale locale = siteRenderingContext.getLocale();
        sink.text(i18n.getString("site-plugin", locale, "report.category.sectionTitle"));
        sink.sectionTitle2_();

        sink.table();

        sink.tableRows();

        String name = i18n.getString("site-plugin", locale, "report.category.column.document");
        String description = i18n.getString("site-plugin", locale, "report.category.column.description");

        sink.tableRow();

        sink.tableHeaderCell();

        sink.text(name);

        sink.tableHeaderCell_();

        sink.tableHeaderCell();

        sink.text(description);

        sink.tableHeaderCell_();

        sink.tableRow_();

        if (categoryReports != null) {
            for (MavenReport report : categoryReports) {
                sink.tableRow();
                sink.tableCell();
                sink.link(report.getOutputName() + ".html");
                sink.text(report.getName(locale));
                sink.link_();
                sink.tableCell_();
                sink.tableCell();
                sink.text(report.getDescription(locale));
                sink.tableCell_();
                sink.tableRow_();
            }
        }

        sink.tableRows_();

        sink.table_();

        sink.section2_();

        sink.section1_();

        sink.body_();

        sink.flush();

        sink.close();

        siteRenderer.mergeDocumentIntoSite(writer, sink, siteRenderingContext);
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
