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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.doxia.site.Menu;
import org.apache.maven.doxia.site.MenuItem;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.DocumentRenderingContext;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.doxia.tools.SiteToolException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.site.descriptor.AbstractSiteDescriptorMojo;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.reporting.exec.MavenReportExecution;
import org.apache.maven.reporting.exec.MavenReportExecutor;
import org.apache.maven.reporting.exec.MavenReportExecutorRequest;
import org.apache.maven.shared.utils.WriterFactory;
import org.codehaus.plexus.util.ReaderFactory;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Base class for site rendering mojos.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public abstract class AbstractSiteRenderingMojo extends AbstractSiteDescriptorMojo {
    /**
     * Module type exclusion mappings
     * ex: <code>fml  -> **&#47;*-m1.fml</code>  (excludes fml files ending in '-m1.fml' recursively)
     * <p/>
     * The configuration looks like this:
     * <pre>
     *   &lt;moduleExcludes&gt;
     *     &lt;moduleType&gt;filename1.ext,**&#47;*sample.ext&lt;/moduleType&gt;
     *     &lt;!-- moduleType can be one of 'apt', 'fml' or 'xdoc'. --&gt;
     *     &lt;!-- The value is a comma separated list of           --&gt;
     *     &lt;!-- filenames or fileset patterns.                   --&gt;
     *     &lt;!-- Here's an example:                               --&gt;
     *     &lt;xdoc&gt;changes.xml,navigation.xml&lt;/xdoc&gt;
     *   &lt;/moduleExcludes&gt;
     * </pre>
     */
    @Parameter
    private Map<String, String> moduleExcludes;

    /**
     * Additional template properties for rendering the site. See
     * <a href="/doxia/doxia-sitetools/doxia-site-renderer/">Doxia Site Renderer</a>.
     */
    @Parameter
    private Map<String, Object> attributes;

    /**
     * Site renderer.
     */
    @Component
    protected SiteRenderer siteRenderer;

    /**
     * Directory containing generated documentation in source format (Doxia supported markup).
     * This is used to pick up other source docs that might have been generated at build time (by reports or any other
     * build time mean).
     * This directory is expected to have the same structure as <code>siteDirectory</code>
     * (ie. one directory per Doxia-source-supported markup types).
     *
     * todo should we deprecate in favour of reports directly using Doxia Sink API, without this Doxia source
     * intermediate step?
     */
    @Parameter(alias = "workingDirectory", defaultValue = "${project.build.directory}/generated-site")
    protected File generatedSiteDirectory;

    /**
     * The current Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession mavenSession;

    /**
     * The mojo execution
     */
    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    protected MojoExecution mojoExecution;

    /**
     * replaces previous reportPlugins parameter, that was injected by Maven core from
     * reporting section: but this new configuration format has been abandoned.
     *
     * @since 3.7.1
     */
    @Parameter(defaultValue = "${project.reporting}", readonly = true)
    private Reporting reporting;

    /**
     * Whether to generate the summary page for project reports: project-info.html.
     *
     * @since 2.3
     */
    @Parameter(property = "generateProjectInfo", defaultValue = "true")
    private boolean generateProjectInfo;

    /**
     * Generate a sitemap. The result will be a "sitemap.html" file at the site root.
     *
     * @since 2.1
     */
    @Parameter(property = "generateSitemap", defaultValue = "false")
    private boolean generateSitemap;

    /**
     * Specifies the input encoding.
     *
     * @since 2.3
     */
    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}")
    private String inputEncoding;

    /**
     * Specifies the output encoding.
     *
     * @since 2.3
     */
    @Parameter(property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}")
    private String outputEncoding;

    /**
     * Timestamp for reproducible output archive entries, either formatted as ISO 8601
     * <code>yyyy-MM-dd'T'HH:mm:ssXXX</code> or as an int representing seconds since the epoch (like
     * <a href="https://reproducible-builds.org/docs/source-date-epoch/">SOURCE_DATE_EPOCH</a>).
     *
     * @since 3.9.0
     */
    @Parameter(defaultValue = "${project.build.outputTimestamp}")
    protected String outputTimestamp;

    @Component
    protected MavenReportExecutor mavenReportExecutor;

    /**
     * Gets the input files encoding.
     *
     * @return The input files encoding, never <code>null</code>.
     */
    protected String getInputEncoding() {
        return (inputEncoding == null || inputEncoding.isEmpty()) ? ReaderFactory.FILE_ENCODING : inputEncoding;
    }

    /**
     * Gets the effective reporting output files encoding.
     *
     * @return The effective reporting output file encoding, never <code>null</code>.
     */
    protected String getOutputEncoding() {
        return (outputEncoding == null) ? WriterFactory.UTF_8 : outputEncoding;
    }

    /**
     * Whether to save Velocity processed Doxia content (<code>*.&lt;ext&gt;.vm</code>)
     * to <code>${generatedSiteDirectory}/processed</code>.
     *
     * @since 3.5
     */
    @Parameter
    private boolean saveProcessedContent;

    protected void checkInputEncoding() {
        if (inputEncoding == null || inputEncoding.isEmpty()) {
            getLog().warn("Input file encoding has not been set, using platform encoding " + ReaderFactory.FILE_ENCODING
                    + ", i.e. build is platform dependent!");
        }
    }

    protected List<MavenReportExecution> getReports(File outputDirectory) throws MojoExecutionException {
        MavenReportExecutorRequest mavenReportExecutorRequest = new MavenReportExecutorRequest();
        mavenReportExecutorRequest.setMavenSession(mavenSession);
        mavenReportExecutorRequest.setExecutionId(mojoExecution.getExecutionId());
        mavenReportExecutorRequest.setProject(project);
        mavenReportExecutorRequest.setReportPlugins(getReportingPlugins());

        List<MavenReportExecution> allReports = mavenReportExecutor.buildMavenReports(mavenReportExecutorRequest);

        // filter out reports that can't be generated
        List<MavenReportExecution> reportExecutions = new ArrayList<>(allReports.size());
        for (MavenReportExecution exec : allReports) {
            String reportMojoInfo = exec.getPlugin().getId() + ":" + exec.getGoal();
            exec.getMavenReport().setReportOutputDirectory(outputDirectory);
            try {
                if (exec.canGenerateReport()) {
                    reportExecutions.add(exec);
                } else if (exec.isUserDefined()) {
                    getLog().info("Skipping " + reportMojoInfo + " report");
                }
            } catch (MavenReportException e) {
                throw new MojoExecutionException(
                        "Failed to determine whether report '" + reportMojoInfo + "' can be generated", e);
            }
        }
        return reportExecutions;
    }

    /**
     * Get the report plugins from reporting section, adding if necessary (i.e. not excluded)
     * default reports (i.e. maven-project-info-reports)
     *
     * @return the effective list of reports
     * @since 3.7.1
     */
    private ReportPlugin[] getReportingPlugins() {
        List<ReportPlugin> reportingPlugins = reporting.getPlugins();

        // MSITE-806: add default report plugin like done in maven-model-builder DefaultReportingConverter
        boolean hasMavenProjectInfoReportsPlugin = false;
        for (ReportPlugin plugin : reportingPlugins) {
            if ("org.apache.maven.plugins".equals(plugin.getGroupId())
                    && "maven-project-info-reports-plugin".equals(plugin.getArtifactId())) {
                hasMavenProjectInfoReportsPlugin = true;
                break;
            }
        }

        if (!reporting.isExcludeDefaults() && !hasMavenProjectInfoReportsPlugin) {
            ReportPlugin mpir = new ReportPlugin();
            mpir.setArtifactId("maven-project-info-reports-plugin");
            reportingPlugins.add(mpir);
        }
        return reportingPlugins.toArray(new ReportPlugin[0]);
    }

    protected SiteRenderingContext createSiteRenderingContext(Locale locale)
            throws MojoExecutionException, IOException, MojoFailureException {
        SiteModel siteModel = prepareSiteModel(locale);
        Map<String, Object> templateProperties = new HashMap<>();
        templateProperties.put("project", project);
        templateProperties.put("inputEncoding", getInputEncoding());
        templateProperties.put("outputEncoding", getOutputEncoding());

        // Put any of the properties in directly into the Velocity context
        for (Map.Entry<Object, Object> entry : project.getProperties().entrySet()) {
            templateProperties.put((String) entry.getKey(), entry.getValue());
        }

        // Comes last if someone wants to deliberately override any default or model properties
        if (attributes != null) {
            templateProperties.putAll(attributes);
        }

        SiteRenderingContext context;
        try {
            Artifact skinArtifact =
                    siteTool.getSkinArtifactFromRepository(repoSession, remoteProjectRepositories, siteModel.getSkin());

            getLog().info(buffer().a("Rendering content with ")
                    .strong(skinArtifact.getId() + " skin")
                    .toString());

            context = siteRenderer.createContextForSkin(
                    skinArtifact, templateProperties, siteModel, project.getName(), locale);
        } catch (SiteToolException e) {
            throw new MojoExecutionException("Failed to retrieve skin artifact from repository", e);
        } catch (RendererException e) {
            throw new MojoExecutionException("Failed to create context for skin", e);
        }

        // Add publish date
        MavenArchiver.parseBuildOutputTimestamp(outputTimestamp).ifPresent(v -> {
            context.setPublishDate(Date.from(v));
        });

        // Generate static site
        context.setRootDirectory(project.getBasedir());
        if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
            context.addSiteDirectory(new File(siteDirectory, locale.toString()));
        } else {
            context.addSiteDirectory(siteDirectory);
        }

        if (moduleExcludes != null) {
            context.setModuleExcludes(moduleExcludes);
        }

        if (saveProcessedContent) {
            File processedDir = new File(generatedSiteDirectory, "processed");
            if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
                context.setProcessedContentOutput(new File(processedDir, locale.toString()));
            } else {
                context.setProcessedContentOutput(processedDir);
            }
        }

        return context;
    }

    /**
     * Go through the list of reports and process each one like this:
     * <ul>
     * <li>Add the report to a map of reports keyed by filename having the report itself as value
     * <li>If the report is not yet in the map of documents, add it together with a suitable renderer
     * </ul>
     *
     * @param reports A List of MavenReports
     * @param documents A Map of documents, keyed by filename
     * @param locale the Locale the reports are processed for.
     * @return A map with all reports keyed by filename having the report itself as value.
     * The map will be used to populate a menu.
     */
    protected Map<String, MavenReport> locateReports(
            List<MavenReportExecution> reports, Map<String, DocumentRenderer> documents, Locale locale) {
        Map<String, MavenReport> reportsByOutputName = new LinkedHashMap<>();
        for (MavenReportExecution mavenReportExecution : reports) {
            MavenReport report = mavenReportExecution.getMavenReport();

            String outputName = report.getOutputName();
            String filename = outputName + ".html";

            // Always add the report to the menu, see MSITE-150
            reportsByOutputName.put(outputName, report);

            if (documents.containsKey(filename)) {
                String reportMojoInfo = mavenReportExecution.getGoal() == null
                        ? ""
                        : (" (" + mavenReportExecution.getPlugin().getArtifactId() + ':'
                                + mavenReportExecution.getPlugin().getVersion() + ':' + mavenReportExecution.getGoal()
                                + ')');

                getLog().info("Skipped \"" + report.getName(locale) + "\" report" + reportMojoInfo + ", file \""
                        + filename + "\" already exists.");
            } else {
                File localizedSiteDirectory;
                if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
                    localizedSiteDirectory = new File(siteDirectory, locale.toString());
                } else {
                    localizedSiteDirectory = siteDirectory;
                }
                String generator = mavenReportExecution.getGoal() == null
                        ? null
                        : mavenReportExecution.getPlugin().getId() + ':' + mavenReportExecution.getGoal();
                DocumentRenderingContext docRenderingContext =
                        new DocumentRenderingContext(localizedSiteDirectory, outputName, generator);
                DocumentRenderer docRenderer =
                        new ReportDocumentRenderer(mavenReportExecution, docRenderingContext, getLog());
                documents.put(filename, docRenderer);
            }
        }
        return reportsByOutputName;
    }

    /**
     * Go through the collection of reports and put each report into a list for the appropriate category. The list is
     * put into a map keyed by the name of the category.
     *
     * @param reports A Collection of MavenReports
     * @return A map keyed category having the report itself as value
     */
    protected Map<String, List<MavenReport>> categoriseReports(Collection<MavenReport> reports) {
        Map<String, List<MavenReport>> categories = new LinkedHashMap<>();
        for (MavenReport report : reports) {
            List<MavenReport> categoryReports = categories.get(report.getCategoryName());
            if (categoryReports == null) {
                categoryReports = new ArrayList<>();
                categories.put(report.getCategoryName(), categoryReports);
            }
            categoryReports.add(report);
        }
        return categories;
    }

    /**
     * Locate every document to be rendered for given locale:<ul>
     * <li>handwritten content, ie Doxia files,</li>
     * <li>reports,</li>
     * <li>"Project Information" and "Project Reports" category summaries.</li>
     * </ul>
     *
     * @param context the site context
     * @param reports the documents
     * @param locale the locale
     * @return the documents and their renderers
     * @throws IOException in case of file reading issue
     * @throws RendererException in case of Doxia rendering issue
     * @see CategorySummaryDocumentRenderer
     */
    protected Map<String, DocumentRenderer> locateDocuments(
            SiteRenderingContext context, List<MavenReportExecution> reports, Locale locale)
            throws IOException, RendererException {
        Map<String, DocumentRenderer> documents = siteRenderer.locateDocumentFiles(context, true);

        Map<String, MavenReport> reportsByOutputName = locateReports(reports, documents, locale);

        // TODO: I want to get rid of categories eventually. There's no way to add your own in a fully i18n manner
        Map<String, List<MavenReport>> categories = categoriseReports(reportsByOutputName.values());

        siteTool.populateReportsMenu(context.getSiteModel(), locale, categories);
        populateReportItems(context.getSiteModel(), locale, reportsByOutputName);

        File localizedSiteDirectory;
        if (!locale.equals(SiteTool.DEFAULT_LOCALE)) {
            localizedSiteDirectory = new File(siteDirectory, locale.toString());
        } else {
            localizedSiteDirectory = siteDirectory;
        }

        if (categories.containsKey(MavenReport.CATEGORY_PROJECT_INFORMATION) && generateProjectInfo) {
            // add "Project Information" category summary document
            List<MavenReport> categoryReports = categories.get(MavenReport.CATEGORY_PROJECT_INFORMATION);
            MojoExecution subMojoExecution =
                    new MojoExecution(mojoExecution.getPlugin(), "project-info", mojoExecution.getExecutionId());
            DocumentRenderingContext docRenderingContext = new DocumentRenderingContext(
                    localizedSiteDirectory,
                    subMojoExecution.getGoal(),
                    subMojoExecution.getPlugin().getId() + ':' + subMojoExecution.getGoal());
            String title = i18n.getString("site-plugin", locale, "report.information.title");
            String desc1 = i18n.getString("site-plugin", locale, "report.information.description1");
            String desc2 = i18n.getString("site-plugin", locale, "report.information.description2");
            DocumentRenderer docRenderer = new CategorySummaryDocumentRenderer(
                    subMojoExecution, docRenderingContext, title, desc1, desc2, i18n, categoryReports, getLog());

            String filename = docRenderer.getOutputName();
            if (!documents.containsKey(filename)) {
                documents.put(filename, docRenderer);
            } else {
                getLog().info("Skipped \"" + title + "\" report; file \"" + filename + "\" already exists.");
            }
        }

        if (categories.containsKey(MavenReport.CATEGORY_PROJECT_REPORTS)) {
            // add "Project Reports" category summary document
            List<MavenReport> categoryReports = categories.get(MavenReport.CATEGORY_PROJECT_REPORTS);
            MojoExecution subMojoExecution =
                    new MojoExecution(mojoExecution.getPlugin(), "project-reports", mojoExecution.getExecutionId());
            DocumentRenderingContext docRenderingContext = new DocumentRenderingContext(
                    localizedSiteDirectory,
                    subMojoExecution.getGoal(),
                    subMojoExecution.getPlugin().getId() + ':' + subMojoExecution.getGoal());
            String title = i18n.getString("site-plugin", locale, "report.project.title");
            String desc1 = i18n.getString("site-plugin", locale, "report.project.description1");
            String desc2 = i18n.getString("site-plugin", locale, "report.project.description2");
            DocumentRenderer docRenderer = new CategorySummaryDocumentRenderer(
                    subMojoExecution, docRenderingContext, title, desc1, desc2, i18n, categoryReports, getLog());

            String filename = docRenderer.getOutputName();
            if (!documents.containsKey(filename)) {
                documents.put(filename, docRenderer);
            } else {
                getLog().info("Skipped \"" + title + "\" report; file \"" + filename + "\" already exists.");
            }
        }

        if (generateSitemap) {
            MojoExecution subMojoExecution =
                    new MojoExecution(mojoExecution.getPlugin(), "sitemap", mojoExecution.getExecutionId());
            DocumentRenderingContext docRenderingContext = new DocumentRenderingContext(
                    localizedSiteDirectory,
                    subMojoExecution.getGoal(),
                    subMojoExecution.getPlugin().getId() + ':' + subMojoExecution.getGoal());
            String title = i18n.getString("site-plugin", locale, "site.sitemap.title");
            DocumentRenderer docRenderer = new SitemapDocumentRenderer(
                    subMojoExecution, docRenderingContext, title, context.getSiteModel(), i18n, getLog());

            String filename = docRenderer.getOutputName();
            if (!documents.containsKey(filename)) {
                documents.put(filename, docRenderer);
            } else {
                getLog().info("Skipped \"" + title + "\" report; file \"" + filename + "\" already exists.");
            }
        }

        return documents;
    }

    protected void populateReportItems(
            SiteModel siteModel, Locale locale, Map<String, MavenReport> reportsByOutputName) {
        for (Menu menu : siteModel.getMenus()) {
            populateItemRefs(menu.getItems(), locale, reportsByOutputName);
        }
    }

    private void populateItemRefs(List<MenuItem> items, Locale locale, Map<String, MavenReport> reportsByOutputName) {
        for (Iterator<MenuItem> i = items.iterator(); i.hasNext(); ) {
            MenuItem item = i.next();

            if (item.getRef() != null) {
                MavenReport report = reportsByOutputName.get(item.getRef());

                if (report != null) {
                    if (item.getName() == null) {
                        item.setName(report.getName(locale));
                    }

                    if (item.getHref() == null || item.getHref().length() == 0) {
                        item.setHref(report.getOutputName() + ".html");
                    }
                } else {
                    getLog().warn("Unrecognised reference: '" + item.getRef() + "'");
                    i.remove();
                }
            }

            populateItemRefs(item.getItems(), locale, reportsByOutputName);
        }
    }
}
