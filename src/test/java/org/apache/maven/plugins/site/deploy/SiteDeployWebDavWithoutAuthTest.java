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

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.site.stubs.SiteMavenProjectStub;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Lamy
 * @since 3.0-beta-2
 *
 */
public class SiteDeployWebDavWithoutAuthTest {
    @TempDir
    private static File directory;

    private static File siteTargetPath;
    private static SimpleDavServerHandler simpleDavServerHandler;

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }

    @BeforeAll
    public static void prepare() throws Exception {
        siteTargetPath = new File(newFolder(directory, "junit"), "target");
        if (!siteTargetPath.exists()) {
            siteTargetPath.mkdirs();
        }
        simpleDavServerHandler = new SimpleDavServerHandler(siteTargetPath);
    }

    @AfterEach
    public void cleanup() throws Exception {
        simpleDavServerHandler.httpRequests.clear();
        FileUtils.cleanDirectory(siteTargetPath);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        simpleDavServerHandler.stop();
    }

    @Nested
    @MojoTest
    class NoAutzDavDeploy {

        @Inject
        private Settings settings;

        @Inject
        private MavenProject project;

        @InjectMojo(goal = "deploy", pom = "src/test/resources/unit/deploy-dav/pom.xml")
        @MojoParameter(name = "inputDirectory", value = "target/site")
        @Test
        public void noAuthzDavDeploy(SiteDeployMojo mojo) throws Exception {
            assertTrue(
                    simpleDavServerHandler.getPort() > 0,
                    "dav server port not available: " + simpleDavServerHandler.getPort());

            mojo.execute();
            assertContentInFiles();
            assertFalse(assertAtLeastOneRequestContainsHeader(simpleDavServerHandler.httpRequests, "Proxy-Connection"));
        }

        @Provides
        private Settings settings() {
            return new Settings();
        }

        @Provides
        private MavenProject project() {
            SiteMavenProjectStub siteMavenProjectStub = new SiteMavenProjectStub("deploy-dav");
            siteMavenProjectStub
                    .getDistributionManagement()
                    .getSite()
                    .setUrl("dav:http://localhost:" + simpleDavServerHandler.getPort() + "/site/");
            return siteMavenProjectStub;
        }
    }

    @Nested
    @MojoTest
    class DavDeployThruProxyWithoutAuthzInProxy {

        @Inject
        private MavenProject project;

        @Inject
        private Settings settings;

        @Inject
        private MavenSession mavenSession;

        @InjectMojo(goal = "deploy", pom = "src/test/resources/unit/deploy-dav/pom.xml")
        @MojoParameter(name = "inputDirectory", value = "target/site")
        @Test
        public void davDeployThruProxyWithoutAuthzInProxy(SiteDeployMojo mojo) throws Exception {
            when(mavenSession.getRequest()).thenReturn(request());

            mojo.execute();

            assertContentInFiles();

            assertTrue(assertAtLeastOneRequestContainsHeader(simpleDavServerHandler.httpRequests, "Proxy-Connection"));
        }

        @Provides
        private MavenProject project() {
            SiteMavenProjectStub siteMavenProjectStub = new SiteMavenProjectStub("deploy-dav");
            // olamy, Note : toto is something like foo or bar for french folks :-)
            String siteUrl = "dav:http://toto.com/site/";
            siteMavenProjectStub.getDistributionManagement().getSite().setUrl(siteUrl);

            return siteMavenProjectStub;
        }

        @Provides
        private Settings settings() {
            Settings settings = new Settings();

            settings.addProxy(proxy());
            return settings;
        }

        private MavenExecutionRequest request() {
            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            request.setProxies(Arrays.asList(proxy()));
            return request;
        }

        private Proxy proxy() {
            Proxy proxy = new Proxy();

            // dummy proxy
            proxy.setActive(true);
            proxy.setHost("localhost");
            proxy.setPort(simpleDavServerHandler.getPort());
            proxy.setProtocol("http");
            proxy.setNonProxyHosts("www.google.com|*.somewhere.com");
            return proxy;
        }
    }

    private void assertContentInFiles() throws Exception {
        File htmlFile = new File(siteTargetPath, "site" + File.separator + "index.html");
        assertTrue(htmlFile.exists());
        String htmlContent = new String(Files.readAllBytes(htmlFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(htmlContent.contains("Welcome to Apache Maven"));

        File cssFile = new File(siteTargetPath, "site" + File.separator + "css" + File.separator + "maven-base.css");
        assertTrue(cssFile.exists());
        String cssContent = new String(Files.readAllBytes(cssFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(cssContent.contains("background-image: url(../images/collapsed.gif);"));
    }

    private boolean assertAtLeastOneRequestContainsHeader(List<HttpRequest> requests, String headerName) {
        for (HttpRequest rq : requests) {
            boolean containsProxyHeader = rq.headers.containsKey(headerName);
            if (containsProxyHeader) {
                return true;
            }
        }
        return false;
    }
}
