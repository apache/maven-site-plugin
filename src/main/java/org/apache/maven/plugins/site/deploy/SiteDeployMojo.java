package org.apache.maven.plugins.site.deploy;

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

import org.apache.maven.model.Site;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Deploys the generated site using <a href="/wagon/">wagon supported
 * protocols</a> to the site URL specified in the
 * <code>&lt;distributionManagement&gt;</code> section of the POM.
 * <p>
 * For <code>scp</code> protocol, the website files are packaged by wagon into zip archive,
 * then the archive is transfered to the remote host, next it is un-archived which is much faster
 * than making a file by file copy.
 * </p>
 *
 * @author <a href="mailto:michal@org.codehaus.org">Michal Maczka</a>
 *
 * @since 2.0
 */
@Mojo( name = "deploy" )
public class SiteDeployMojo
    extends AbstractDeployMojo
{
    @Override
    protected boolean isDeploy()
    {
        return true;
    }

    /**
     * Deploy distribution site url is directly the current project value.
     */
    @Override
    protected String determineTopDistributionManagementSiteUrl()
        throws MojoExecutionException
    {
        return getDeploySite().getUrl();
    }

    /**
     * Deploy directly to the current project's distribution management site.
     */
    @Override
    protected Site determineDeploySite()
        throws MojoExecutionException
    {
        return getSite( getTopLevelProject( project ) );
    }
}
