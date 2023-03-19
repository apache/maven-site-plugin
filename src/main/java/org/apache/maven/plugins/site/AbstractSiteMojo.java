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
package org.apache.maven.plugins.site;

import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.i18n.I18N;

/**
 * Base class for site mojos.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public abstract class AbstractSiteMojo extends AbstractMojo {
    /**
     * A comma separated list of locales to render. The first valid token will be the default Locale
     * for this site.
     *
     * @since 2.3
     */
    @Parameter(property = "locales", defaultValue = "default")
    private String locales;

    /**
     * Set this to 'true' to skip site generation and staging.
     *
     * @since 3.0
     */
    @Parameter(property = "maven.site.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * SiteTool.
     */
    @Component
    protected SiteTool siteTool;

    /**
     * Internationalization.
     */
    @Component
    protected I18N i18n;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    protected List<Locale> getLocales() {
        return siteTool.getSiteLocales(locales);
    }
}
