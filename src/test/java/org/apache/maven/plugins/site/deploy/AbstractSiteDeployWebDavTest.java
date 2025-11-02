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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.bridge.MavenRepositorySystem;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugins.site.stubs.SiteMavenProjectStub;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Lamy
 *
 */
@RunWith(JUnit4.class)
public abstract class AbstractSiteDeployWebDavTest extends AbstractMojoTestCase {

    // Can use @TempDir with JUnit 5
    @Rule
    public TemporaryFolder directory = new TemporaryFolder();

    private File siteTargetPath;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        siteTargetPath = new File(directory.newFolder(), "target");
        if (!siteTargetPath.exists()) {
            siteTargetPath.mkdirs();
        }
    }

    abstract String getMojoName();

    abstract AbstractMojo getMojo(File pomFile) throws Exception;

    @Test
    public void noAuthzDavDeploy() throws Exception {
        SimpleDavServerHandler simpleDavServerHandler = new SimpleDavServerHandler(siteTargetPath);

        try {
            File pomFile = getTestFile("src/test/resources/unit/deploy-dav/pom.xml");
            AbstractMojo mojo = getMojo(pomFile);
            assertNotNull(mojo);
            SiteMavenProjectStub siteMavenProjectStub = new SiteMavenProjectStub("deploy-dav");

            assertTrue(
                    simpleDavServerHandler.getPort() > 0,
                    "dav server port not available: " + simpleDavServerHandler.getPort());

            siteMavenProjectStub
                    .getDistributionManagement()
                    .getSite()
                    .setUrl("dav:http://localhost:" + simpleDavServerHandler.getPort() + "/site/");

            setVariableValueToObject(mojo, "project", siteMavenProjectStub);
            Settings settings = new Settings();
            setVariableValueToObject(mojo, "settings", settings);
            File inputDirectory = new File("src/test/resources/unit/deploy-dav/target/site");

            setVariableValueToObject(mojo, "inputDirectory", inputDirectory);
            mojo.execute();

            assertContentInFiles();
            assertFalse(requestsContainsProxyUse(simpleDavServerHandler.httpRequests));
        } finally {
            simpleDavServerHandler.stop();
        }
    }

    @Test
    public void davDeployThruProxyWithoutAuthzInProxy() throws Exception {
        SimpleDavServerHandler simpleDavServerHandler = new SimpleDavServerHandler(siteTargetPath);
        try {
            File pluginXmlFile = getTestFile("src/test/resources/unit/deploy-dav/pom.xml");
            AbstractMojo mojo = getMojo(pluginXmlFile);
            assertNotNull(mojo);
            SiteMavenProjectStub siteMavenProjectStub = new SiteMavenProjectStub("deploy-dav");
            // olamy, Note : toto is something like foo or bar for french folks :-)
            String siteUrl = "dav:http://toto.com/site/";
            siteMavenProjectStub.getDistributionManagement().getSite().setUrl(siteUrl);

            setVariableValueToObject(mojo, "project", siteMavenProjectStub);
            Settings settings = new Settings();
            Proxy proxy = new Proxy();

            // dummy proxy
            proxy.setActive(true);
            proxy.setHost("localhost");
            proxy.setPort(simpleDavServerHandler.getPort());
            proxy.setProtocol("http");
            proxy.setNonProxyHosts("www.google.com|*.somewhere.com");
            settings.addProxy(proxy);

            setVariableValueToObject(mojo, "settings", settings);

            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            request.setProxies(Arrays.asList(proxy));
            MavenSession mavenSession = new MavenSession(getContainer(), null, request, null);

            setVariableValueToObject(mojo, "mavenSession", mavenSession);

            File inputDirectory = new File("src/test/resources/unit/deploy-dav/target/site");

            setVariableValueToObject(mojo, "inputDirectory", inputDirectory);
            mojo.execute();

            assertContentInFiles();

            assertTrue(requestsContainsProxyUse(simpleDavServerHandler.httpRequests));
        } finally {
            simpleDavServerHandler.stop();
        }
    }

    @Test
    public void davDeployThruProxyWitAuthzInProxy() throws Exception {
        Map<String, String> authentications = new HashMap<>();
        authentications.put("foo", "titi");

        AuthAsyncProxyServlet servlet = new AuthAsyncProxyServlet(authentications, siteTargetPath);

        SimpleDavServerHandler simpleDavServerHandler = new SimpleDavServerHandler(servlet);
        try {
            File pluginXmlFile = getTestFile("src/test/resources/unit/deploy-dav/pom.xml");
            AbstractMojo mojo = getMojo(pluginXmlFile);
            assertNotNull(mojo);
            SiteMavenProjectStub siteMavenProjectStub = new SiteMavenProjectStub("deploy-dav");

            siteMavenProjectStub.getDistributionManagement().getSite().setUrl("dav:http://toto.com/site/");

            setVariableValueToObject(mojo, "project", siteMavenProjectStub);
            Settings settings = new Settings();
            Proxy proxy = new Proxy();

            // dummy proxy
            proxy.setActive(true);
            proxy.setHost("localhost");
            proxy.setPort(simpleDavServerHandler.getPort());
            proxy.setProtocol("dav");
            proxy.setUsername("foo");
            proxy.setPassword("titi");
            proxy.setNonProxyHosts("www.google.com|*.somewhere.com");
            settings.addProxy(proxy);

            setVariableValueToObject(mojo, "settings", settings);

            MavenExecutionRequest request = new DefaultMavenExecutionRequest();
            request.setProxies(Arrays.asList(proxy));
            MavenSession mavenSession = new MavenSession(getContainer(), null, request, null);

            setVariableValueToObject(mojo, "mavenSession", mavenSession);

            File inputDirectory = new File("src/test/resources/unit/deploy-dav/target/site");

            // test which mojo we are using
            if (ReflectionUtils.getFieldByNameIncludingSuperclasses("inputDirectory", mojo.getClass()) != null) {
                setVariableValueToObject(mojo, "inputDirectory", inputDirectory);
            } else {
                setVariableValueToObject(mojo, "stagingDirectory", inputDirectory);
                setVariableValueToObject(mojo, "reactorProjects", Collections.emptyList());
                setVariableValueToObject(
                        mojo,
                        "localRepository",
                        MavenRepositorySystem.createArtifactRepository(
                                "local", "foo", new DefaultRepositoryLayout(), null, null));
                setVariableValueToObject(mojo, "siteTool", getContainer().lookup(SiteTool.class));
                setVariableValueToObject(mojo, "siteDirectory", new File("foo"));
                setVariableValueToObject(mojo, "remoteProjectRepositories", Collections.emptyList());
            }
            mojo.execute();

            assertContentInFiles();
            assertTrue(requestsContainsProxyUse(servlet.httpRequests));
            assertAtLeastOneRequestContainsHeader(servlet.httpRequests, "Proxy-Authorization");
        } finally {
            simpleDavServerHandler.stop();
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

    /**
     * @param requests
     * @return true if at least on request use proxy http header Proxy-Connection : Keep-Alive
     */
    private boolean requestsContainsProxyUse(List<HttpRequest> requests) {
        return assertAtLeastOneRequestContainsHeader(requests, "Proxy-Connection");
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
