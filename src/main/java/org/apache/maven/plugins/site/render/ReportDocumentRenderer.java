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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenMultiPageReport;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.reporting.exec.MavenReportExecution;
import org.codehaus.plexus.util.PathTool;
import org.codehaus.plexus.util.WriterFactory;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Renders a Maven report in a Doxia site.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see org.apache.maven.doxia.siterenderer.DoxiaDocumentRenderer
 */
public class ReportDocumentRenderer implements DocumentRenderer {
    private final MavenReport report;

    private final DocumentRenderingContext docRenderingContext;

    private final String reportMojoInfo;

    private final ClassLoader classLoader;

    private final Log log;

    public ReportDocumentRenderer(
            MavenReportExecution mavenReportExecution, DocumentRenderingContext docRenderingContext, Log log) {
        this.report = mavenReportExecution.getMavenReport();
        this.docRenderingContext = docRenderingContext;
        this.reportMojoInfo = mavenReportExecution.getGoal() == null
                ? null
                : mavenReportExecution.getPlugin().getArtifactId()
                        + ':'
                        + mavenReportExecution.getPlugin().getVersion()
                        + ':'
                        + mavenReportExecution.getGoal();
        this.classLoader = mavenReportExecution.getClassLoader();
        this.log = log;
    }

    private static class MultiPageSubSink extends SiteRendererSink {
        private File outputDirectory;

        private String outputName;

        MultiPageSubSink(File outputDirectory, String outputName, DocumentRenderingContext docRenderingContext) {
            super(docRenderingContext);
            this.outputName = outputName;
            this.outputDirectory = outputDirectory;
        }

        public String getOutputName() {
            return outputName;
        }

        public File getOutputDirectory() {
            return outputDirectory;
        }
    }

    private static class MultiPageSinkFactory implements SinkFactory {
        /**
         * The report that is (maybe) generating multiple pages
         */
        private MavenReport report;

        /**
         * The main DocumentRenderingContext, which is the base for the DocumentRenderingContext of subpages
         */
        private DocumentRenderingContext docRenderingContext;

        /**
         * List of sinks (subpages) associated to this report
         */
        private List<MultiPageSubSink> sinks = new ArrayList<>();

        MultiPageSinkFactory(MavenReport report, DocumentRenderingContext docRenderingContext) {
            this.report = report;
            this.docRenderingContext = docRenderingContext;
        }

        @Override
        public Sink createSink(File outputDirectory, String outputName) {
            // Create a new document rendering context, similar to the main one, but with a different output name
            String document = PathTool.getRelativeFilePath(
                    report.getReportOutputDirectory().getPath(), new File(outputDirectory, outputName).getPath());
            // Remove .html suffix since we know that we are in Site Renderer context
            document = document.substring(0, document.lastIndexOf('.'));

            DocumentRenderingContext subSinkContext = new DocumentRenderingContext(
                    docRenderingContext.getBasedir(),
                    docRenderingContext.getBasedirRelativePath(),
                    document,
                    docRenderingContext.getParserId(),
                    docRenderingContext.getExtension(),
                    docRenderingContext.isEditable(),
                    docRenderingContext.getGenerator());

            // Create a sink for this subpage, based on this new document rendering context
            MultiPageSubSink sink = new MultiPageSubSink(outputDirectory, outputName, subSinkContext);

            // Add it to the list of sinks associated to this report
            sinks.add(sink);

            return sink;
        }

        @Override
        public Sink createSink(File arg0, String arg1, String arg2) throws IOException {
            // Not used
            return null;
        }

        @Override
        public Sink createSink(OutputStream arg0) throws IOException {
            // Not used
            return null;
        }

        @Override
        public Sink createSink(OutputStream arg0, String arg1) throws IOException {
            // Not used
            return null;
        }

        public List<MultiPageSubSink> sinks() {
            return sinks;
        }
    }

    @Override
    public void renderDocument(Writer writer, SiteRenderer siteRenderer, SiteRenderingContext siteRenderingContext)
            throws RendererException, IOException {
        Locale locale = siteRenderingContext.getLocale();
        String localReportName = report.getName(locale);

        String msg = "Generating \"" + buffer().strong(localReportName) + "\" report";
        // CHECKSTYLE_OFF: MagicNumber
        log.info(
                reportMojoInfo == null
                        ? msg
                        : (StringUtils.rightPad(msg, 40)
                                + buffer().strong(" --- ").mojo(reportMojoInfo)));
        // CHECKSTYLE_ON: MagicNumber

        // main sink
        SiteRendererSink mainSink = new SiteRendererSink(docRenderingContext);
        // sink factory, for multi-page reports that need sub-sinks
        MultiPageSinkFactory multiPageSinkFactory = new MultiPageSinkFactory(report, docRenderingContext);

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(classLoader);
            }

            if (report instanceof MavenMultiPageReport) {
                // extended multi-page API
                ((MavenMultiPageReport) report).generate(mainSink, multiPageSinkFactory, locale);
            } else {
                // old single-page-only API
                report.generate(mainSink, locale);
            }
        } catch (MavenReportException e) {
            String report = (reportMojoInfo == null) ? ('"' + localReportName + '"') : reportMojoInfo;
            throw new RendererException("Error generating " + report + " report", e);
        } catch (RuntimeException re) {
            // MSITE-836: if report generation throws a RuntimeException, transform to RendererException
            String report = (reportMojoInfo == null) ? ('"' + localReportName + '"') : reportMojoInfo;
            throw new RendererException("Error generating " + report + " report", re);
        } catch (LinkageError e) {
            String report = (reportMojoInfo == null) ? ('"' + localReportName + '"') : reportMojoInfo;
            log.warn(
                    "An issue has occurred with " + report + " report, skipping LinkageError " + e.getMessage()
                            + ", please report an issue to Maven dev team.",
                    e);
        } finally {
            if (classLoader != null) {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
            mainSink.close();
        }

        if (report.isExternalReport()) {
            // external reports are rendered from their own: no Doxia site rendering needed
            return;
        }

        // render main sink document content
        siteRenderer.mergeDocumentIntoSite(writer, mainSink, siteRenderingContext);

        // render sub-sinks, eventually created by multi-page reports
        String outputName = "";
        List<MultiPageSubSink> sinks = multiPageSinkFactory.sinks();

        log.debug("Multipage report: " + sinks.size() + " subreports");

        for (MultiPageSubSink mySink : sinks) {
            outputName = mySink.getOutputName();
            log.debug("  Rendering " + outputName);

            // Create directories if necessary
            if (!mySink.getOutputDirectory().exists()) {
                mySink.getOutputDirectory().mkdirs();
            }

            File outputFile = new File(mySink.getOutputDirectory(), outputName);

            try (Writer out = WriterFactory.newWriter(outputFile, siteRenderingContext.getOutputEncoding())) {
                siteRenderer.mergeDocumentIntoSite(out, mySink, siteRenderingContext);
                mySink.close();
                mySink = null;
            } finally {
                if (mySink != null) {
                    mySink.close();
                }
            }
        }
    }

    @Override
    public String getOutputName() {
        return docRenderingContext.getOutputName();
    }

    @Override
    public DocumentRenderingContext getRenderingContext() {
        return docRenderingContext;
    }

    @Override
    public boolean isOverwrite() {
        // TODO: would be nice to query the report to see if it is modified
        return true;
    }

    /**
     * @return true if the current report is external, false otherwise
     */
    @Override
    public boolean isExternalReport() {
        return report.isExternalReport();
    }

    public String getReportMojoInfo() {
        return reportMojoInfo;
    }
}
