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

import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Site;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for AbstractDeployMojo.
 */
public class AbstractDeployMojoTest {

    /**
     * Test that getTopLevelProject correctly handles SCM URLs with different repositories.
     * This is the test case for MSITE-1033.
     */
    @Test
    public void testGetTopLevelProjectWithDifferentScmUrls() throws Exception {
        // Create a mock deploy mojo
        TestDeployMojo mojo = new TestDeployMojo();

        // Create child project with SCM URL
        MavenProject childProject =
                createProjectWithSite("child", "scm:git:git@github.com:codehaus-plexus/plexus-sec-dispatcher.git/");

        // Create parent project with different SCM URL
        MavenProject parentProject =
                createProjectWithSite("parent", "scm:git:https://github.com/codehaus-plexus/plexus-pom.git/");

        // Set up the parent-child relationship
        childProject.setParent(parentProject);

        // Call getTopLevelProject - it should return childProject, not parentProject
        // because the SCM URLs point to different repositories
        MavenProject topProject = mojo.getTopLevelProject(childProject);

        // The top project should be the child project itself since the parent has a different site
        assertEquals("Top project should be child project due to different SCM URLs", childProject, topProject);
    }

    /**
     * Test that getTopLevelProject correctly handles SCM URLs with the same repository.
     */
    @Test
    public void testGetTopLevelProjectWithSameScmUrls() throws Exception {
        // Create a mock deploy mojo
        TestDeployMojo mojo = new TestDeployMojo();

        // Create child project with SCM URL
        MavenProject childProject =
                createProjectWithSite("child", "scm:git:https://github.com/codehaus-plexus/plexus-pom.git/child");

        // Create parent project with same base SCM URL
        MavenProject parentProject =
                createProjectWithSite("parent", "scm:git:https://github.com/codehaus-plexus/plexus-pom.git/");

        // Set up the parent-child relationship
        childProject.setParent(parentProject);

        // Call getTopLevelProject - it should return parentProject
        // because the SCM URLs point to the same repository
        MavenProject topProject = mojo.getTopLevelProject(childProject);

        // The top project should be the parent project since they share the same site
        assertEquals("Top project should be parent project due to same SCM base URL", parentProject, topProject);
    }

    /**
     * Test that getTopLevelProject correctly handles non-SCM URLs.
     */
    @Test
    public void testGetTopLevelProjectWithNonScmUrls() throws Exception {
        // Create a mock deploy mojo
        TestDeployMojo mojo = new TestDeployMojo();

        // Create child project with regular URL
        MavenProject childProject = createProjectWithSite("child", "https://example.com/site/child");

        // Create parent project with same base URL
        MavenProject parentProject = createProjectWithSite("parent", "https://example.com/site/");

        // Set up the parent-child relationship
        childProject.setParent(parentProject);

        // Call getTopLevelProject - it should return parentProject
        MavenProject topProject = mojo.getTopLevelProject(childProject);

        // The top project should be the parent project since they share the same site
        assertEquals("Top project should be parent project for regular URLs", parentProject, topProject);
    }

    /**
     * Test that getTopLevelProject correctly handles SCM URLs with standard https format.
     */
    @Test
    public void testGetTopLevelProjectWithHttpsScmUrls() throws Exception {
        // Create a mock deploy mojo
        TestDeployMojo mojo = new TestDeployMojo();

        // Create child project with https SCM URL
        MavenProject childProject = createProjectWithSite("child", "scm:git:https://github.com/user/repo1.git/");

        // Create parent project with different https SCM URL (different domain)
        MavenProject parentProject = createProjectWithSite("parent", "scm:git:https://gitlab.com/user/repo2.git/");

        // Set up the parent-child relationship
        childProject.setParent(parentProject);

        // Call getTopLevelProject - it should return childProject
        // because the SCM URLs point to different repositories
        MavenProject topProject = mojo.getTopLevelProject(childProject);

        // The top project should be the child project itself since the parent has a different site
        assertEquals("Top project should be child project due to different https SCM URLs", childProject, topProject);
    }

    /**
     * Test that extractComparableUrl properly handles SVN URLs with different schemes but same host.
     * For SVN (hierarchical VCS), URLs with different schemes (http vs https) should be normalized
     * to the same scheme to allow URIPathDescriptor.sameSite() to recognize them as the same site.
     * Note: The paths may differ (one being a subpath of another), but as long as scheme, host, and port
     * are the same, URIPathDescriptor.sameSite() will correctly identify them as the same site.
     */
    @Test
    public void testExtractComparableUrlForSvnUrls() throws Exception {
        // Extract and normalize both URLs
        String url1 = AbstractDeployMojo.extractComparableUrl("scm:svn:http://svn.apache.org/repos/asf/maven/website");
        String url2 = AbstractDeployMojo.extractComparableUrl(
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/components");

        // Both should be normalized to http scheme
        assertEquals("http://svn.apache.org/repos/asf/maven/website", url1);
        assertEquals("http://svn.apache.org/repos/asf/maven/website/components", url2);

        // Now verify they would be considered the same site by creating projects with these URLs
        TestDeployMojo mojo = new TestDeployMojo();
        MavenProject childProject =
                createProjectWithSite("child", "scm:svn:https://svn.apache.org/repos/asf/maven/website/components");
        MavenProject parentProject =
                createProjectWithSite("parent", "scm:svn:http://svn.apache.org/repos/asf/maven/website");
        childProject.setParent(parentProject);

        // The parent should be returned as the top project since they share the same SVN site
        MavenProject topProject = mojo.getTopLevelProject(childProject);
        assertEquals(
                "Top project should be parent due to normalized SVN URLs pointing to same site",
                parentProject,
                topProject);
    }

    private MavenProject createProjectWithSite(String artifactId, String siteUrl) {
        MavenProject project = new MavenProject();
        project.setGroupId("org.apache.maven.test");
        project.setArtifactId(artifactId);
        project.setVersion("1.0");
        project.setName(artifactId);

        DistributionManagement distributionManagement = new DistributionManagement();
        Site site = new Site();
        site.setId("test-site");
        site.setUrl(siteUrl);
        distributionManagement.setSite(site);
        project.setDistributionManagement(distributionManagement);

        return project;
    }

    /**
     * Test implementation of AbstractDeployMojo for testing.
     */
    private static class TestDeployMojo extends AbstractDeployMojo {
        @Override
        protected boolean isDeploy() {
            return true;
        }

        @Override
        protected String determineTopDistributionManagementSiteUrl() throws MojoExecutionException {
            return "test";
        }

        @Override
        protected Site determineDeploySite() throws MojoExecutionException {
            Site site = new Site();
            site.setId("test");
            site.setUrl("test");
            return site;
        }
    }
}
