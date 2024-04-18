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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.doxia.siterenderer.DocumentRenderer;
import org.apache.maven.doxia.siterenderer.DoxiaDocumentRenderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.SiteRenderer;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugins.site.render.ReportDocumentRenderer;
import org.apache.maven.plugins.site.render.SitePluginReportDocumentRenderer;
import org.eclipse.jetty.http.MimeTypes;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;

/**
 * Render a page as requested.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DoxiaFilter implements Filter {
    public static final String OUTPUT_DIRECTORY_KEY = "outputDirectory";

    public static final String SITE_RENDERER_KEY = "siteRenderer";

    public static final String I18N_DOXIA_CONTEXTS_KEY = "i18nDoxiaContexts";

    public static final String LOCALES_LIST_KEY = "localesList";

    private ServletContext servletContext;

    private File outputDirectory;

    private SiteRenderer siteRenderer;

    private Map<String, DoxiaBean> i18nDoxiaContexts;

    private List<Locale> localesList;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        servletContext = filterConfig.getServletContext();

        outputDirectory = (File) servletContext.getAttribute(OUTPUT_DIRECTORY_KEY);

        siteRenderer = (SiteRenderer) servletContext.getAttribute(SITE_RENDERER_KEY);

        i18nDoxiaContexts = (Map<String, DoxiaBean>) servletContext.getAttribute(I18N_DOXIA_CONTEXTS_KEY);

        localesList = (List<Locale>) servletContext.getAttribute(LOCALES_LIST_KEY);
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        // ----------------------------------------------------------------------
        // Handle the servlet path
        // ----------------------------------------------------------------------
        String path = req.getServletPath();
        // welcome file
        if (path.endsWith("/")) {
            path += "index.html";
        }

        // Remove the /
        path = path.substring(1);

        // Handle locale request
        SiteRenderingContext context;
        Map<String, DocumentRenderer> documents;

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
                throw new ServletException("No Doxia bean found for locale '" + localeWanted + "'");
            }
        } else {
            doxiaBean = i18nDoxiaContexts.get("default");
            if (doxiaBean == null) {
                throw new ServletException("No Doxia bean found for the default locale");
            }
        }
        context = doxiaBean.getContext();
        documents = doxiaBean.getDocuments();

        // ----------------------------------------------------------------------
        // Handle report and documents
        // ----------------------------------------------------------------------
        if (documents.containsKey(path)) {
            try {
                DocumentRenderer docRenderer = documents.get(path);
                logDocumentRenderer(path, localeWanted, docRenderer);
                String outputName = docRenderer.getOutputName();
                String contentType = MimeTypes.getDefaultMimeByExtension(outputName);
                if (contentType != null) {
                    servletResponse.setContentType(contentType);
                }
                docRenderer.renderDocument(servletResponse.getWriter(), siteRenderer, context);

                if (docRenderer instanceof ReportDocumentRenderer) {
                    ReportDocumentRenderer reportDocumentRenderer = (ReportDocumentRenderer) docRenderer;
                    if (reportDocumentRenderer.isExternalReport()) {
                        Path externalReportFile = outputDirectory.toPath().resolve(outputName);
                        servletResponse.reset();
                        if (contentType != null) {
                            servletResponse.setContentType(contentType);
                        }
                        Files.copy(externalReportFile, servletResponse.getOutputStream());
                    }
                }

                return;
            } catch (RendererException e) {
                throw new ServletException(e);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
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
        servletContext.log(localizedPath + " -> " + buffer().strong(localizedSource));
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {}
}
