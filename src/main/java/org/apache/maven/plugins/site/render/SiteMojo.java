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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.DoxiaDocumentRenderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.reporting.exec.MavenReportExecution;
import org.apache.maven.shared.utils.logging.MessageBuilder;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Generates the site for a single project.
 * <p>
 * Note that links between module sites in a multi module build will <b>not</b> work, since local build directory
 * structure doesn't match deployed site.
 * </p>
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 *
 */
@Mojo(name = "site", requiresDependencyResolution = ResolutionScope.TEST, requiresReports = true, threadSafe = true)
public class SiteMojo extends AbstractSiteRenderingMojo {
    /**
     * Directory where the project sites and report distributions will be generated (as html/css/...).
     */
    @Parameter(property = "siteOutputDirectory", defaultValue = "${project.reporting.outputDirectory}")
    protected File outputDirectory;

    /**
     * Convenience parameter that allows you to disable report generation.
     */
    @Parameter(property = "generateReports", defaultValue = "true")
    private boolean generateReports;

    /**
     * Whether to validate xml input documents. If set to true, <strong>all</strong> input documents in xml format (in
     * particular xdoc and fml) will be validated and any error will lead to a build failure.
     *
     * @since 2.1.1
     */
    @Parameter(property = "validate", defaultValue = "false")
    private boolean validate;

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("maven.site.skip = true: Skipping site generation");
            return;
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("executing Site Mojo");
        }

        checkInputEncoding();

        try {
            List<Locale> localesList = getLocales();

            for (Locale locale : localesList) {
                getLog().info("Rendering site for "
                        + buffer().strong(
                                        (!locale.equals(SiteTool.DEFAULT_LOCALE)
                                                ? "locale '" + locale + "'"
                                                : "default locale"))
                                .toString());
                File outputDirectory = getOutputDirectory(locale);
                List<MavenReportExecution> reports =
                        generateReports ? getReports(outputDirectory) : Collections.emptyList();
                renderLocale(locale, reports, localesList, outputDirectory);
            }
        } catch (RendererException e) {
            if (e.getCause() instanceof MavenReportException) {
                // issue caused by report, not really by Doxia Site Renderer
                throw new MojoExecutionException(e.getMessage(), e.getCause());
            }
            throw new MojoExecutionException("Failed to render site", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during site generation", e);
        }
    }

    private void renderLocale(
            Locale locale, List<MavenReportExecution> reports, List<Locale> supportedLocales, File outputDirectory)
            throws IOException, RendererException, MojoFailureException, MojoExecutionException {
        SiteRenderingContext context = createSiteRenderingContext(locale);
        context.addSiteLocales(supportedLocales);
        context.setInputEncoding(getInputEncoding());
        context.setOutputEncoding(getOutputEncoding());
        context.setValidate(validate);
        if (validate) {
            getLog().info("Validation is switched on, xml input documents will be validated!");
        }

        // locate all Doxia documents first
        Map<String, DocumentRenderer> documents = locateDocuments(context, reports, locale);

        // copy resources
        siteRenderer.copyResources(context, outputDirectory);

        // and finally render Doxia documents
        List<DocumentRenderer> nonDoxiaDocuments = renderDoxiaDocuments(documents.values(), context, outputDirectory);

        // then non-Doxia documents (e.g. reports)
        renderNonDoxiaDocuments(nonDoxiaDocuments, context, outputDirectory);
    }

    /**
     * Render Doxia documents from the list given, but not reports.
     *
     * @param documents a collection of documents containing both Doxia source files and reports
     * @return the sublist of documents that are not Doxia source files
     */
    private List<DocumentRenderer> renderDoxiaDocuments(
            Collection<DocumentRenderer> documents, SiteRenderingContext context, File outputDirectory)
            throws RendererException, IOException {
        List<DocumentRenderer> doxiaDocuments = new ArrayList<>();
        List<DocumentRenderer> generatedDoxiaDocuments = new ArrayList<>();
        List<DocumentRenderer> nonDoxiaDocuments = new ArrayList<>();

        Map<String, Integer> counts = new TreeMap<>();
        Map<String, Integer> generatedCounts = new TreeMap<>();

        for (DocumentRenderer doc : documents) {
            if (doc instanceof DoxiaDocumentRenderer) {
                DoxiaDocumentRenderer doxia = (DoxiaDocumentRenderer) doc;
                boolean editable = doxia.getRenderingContext().isEditable();

                if (editable) {
                    doxiaDocuments.add(doc);
                } else {
                    generatedDoxiaDocuments.add(doc);
                }

                // count documents per parserId
                String parserId = doxia.getRenderingContext().getParserId();
                Map<String, Integer> actualCounts;
                if (editable) {
                    actualCounts = counts;
                } else {
                    actualCounts = generatedCounts;
                }
                Integer count = actualCounts.get(parserId);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                actualCounts.put(parserId, count);
            } else {
                nonDoxiaDocuments.add(doc);
            }
        }

        if (doxiaDocuments.size() > 0) {
            MessageBuilder mb = buffer();
            mb.a("Rendering ");
            mb.strong(doxiaDocuments.size() + " Doxia document" + (doxiaDocuments.size() > 1 ? "s" : ""));
            mb.a(": ");

            boolean first = true;
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    mb.a(", ");
                }
                mb.strong(entry.getValue() + " " + entry.getKey());
            }

            getLog().info(mb.toString());

            siteRenderer.render(doxiaDocuments, context, outputDirectory);
        }

        if (generatedDoxiaDocuments.size() > 0) {
            MessageBuilder mb = buffer();
            mb.a("Rendering ");
            mb.strong(generatedDoxiaDocuments.size() + " generated Doxia document"
                    + (generatedDoxiaDocuments.size() > 1 ? "s" : ""));
            mb.a(": ");

            boolean first = true;
            for (Map.Entry<String, Integer> entry : generatedCounts.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    mb.a(", ");
                }
                mb.strong(entry.getValue() + " " + entry.getKey());
            }

            getLog().info(mb.toString());

            siteRenderer.render(generatedDoxiaDocuments, context, outputDirectory);
        }

        return nonDoxiaDocuments;
    }

    /**
     * Render non-Doxia documents (e.g., reports) from the list given
     *
     * @param documents a collection of documents containing non-Doxia source files
     */
    private void renderNonDoxiaDocuments(
            Collection<DocumentRenderer> documents, SiteRenderingContext context, File outputDirectory)
            throws RendererException, IOException {
        Map<String, Integer> counts = new TreeMap<>();

        for (DocumentRenderer doc : documents) {
            String type;
            if (doc instanceof ReportDocumentRenderer || doc instanceof SitePluginReportDocumentRenderer) {
                type = "report";
            } else {
                type = "other";
            }

            Integer count = counts.get(type);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            counts.put(type, count);
        }

        if (documents.size() > 0) {
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                String type = entry.getKey();
                Integer count = entry.getValue();

                MessageBuilder mb = buffer();
                mb.a("Rendering ");
                mb.strong(count + " " + type + " document" + (count > 1 ? "s" : ""));

                getLog().info(mb.toString());
            }

            siteRenderer.render(documents, context, outputDirectory);
        }
    }

    private File getOutputDirectory(Locale locale) {
        File file;
        if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
            file = new File(outputDirectory, locale.toString());
        } else {
            file = outputDirectory;
        }

        // Safety
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public MavenProject getProject() {
        return project;
    }

    public MavenSession getSession() {
        return mavenSession;
    }
}
