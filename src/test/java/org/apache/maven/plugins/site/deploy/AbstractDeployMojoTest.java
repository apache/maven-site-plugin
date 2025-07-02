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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class AbstractDeployMojoTest {

    @Test
    public void testIsSameSite() {
        assertTrue(AbstractDeployMojo.isSameSite(
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/components/${maven.site.path}",
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/components/${maven.site.path}"));
        assertTrue(AbstractDeployMojo.isSameSite(
                "https://svn.apache.org/repos/asf/maven/website/components/${maven.site.path}",
                "https://svn.apache.org/repos/asf/maven/website/components/${maven.site.path}"));
        // different scheme (encapsulated in SCM URL) and subpath
        assertTrue(AbstractDeployMojo.isSameSite(
                "scm:svn:http://svn.apache.org/repos/asf/maven/website",
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/components"));
        assertTrue(AbstractDeployMojo.isSameSite(
                "scm:git:ssh://github.com/codehaus-plexus/plexus-pom.git/",
                "scm:git:ssh://github.com/codehaus-plexus/plexus-pom.git/"));
        assertTrue(
                AbstractDeployMojo.isSameSite(
                        "file:////Users/konradwindszus/git/maven/maven-site-plugin/target/it/site-inheritance/webhost.company.com/deploy/www/website/module",
                        "file:////Users/konradwindszus/git/maven/maven-site-plugin/target/it/site-inheritance/webhost.company.com/deploy/www/website/module/submodule"));
        // sibling paths
        assertFalse(AbstractDeployMojo.isSameSite(
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/a/${maven.site.path}",
                "scm:svn:https://svn.apache.org/repos/asf/maven/website/b/${maven.site.path}"));
        // sibling paths
        assertFalse(AbstractDeployMojo.isSameSite(
                "scm:git:ssh://github.com/codehaus-plexus/plexus-pom.git/",
                "scm:git:ssh://git@github.com/codehaus-plexus/plexus-sec-dispatcher.git/"));
        // SCM URLs which are opaque (i.e. non hierarchical URIs)
        assertThrows(
                IllegalArgumentException.class,
                () -> AbstractDeployMojo.isSameSite(
                        "scm:local:/usr/modules:my_module", "scm:local:/usr/modules:my_module"));
    }
}
