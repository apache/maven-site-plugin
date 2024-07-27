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
package org.apache.maven.plugins.site.stubs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Site;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 *
 */
public class SiteMavenProjectStub extends MavenProjectStub {
    private File basedir;

    DistributionManagement distributionManagement = new DistributionManagement();

    public SiteMavenProjectStub(String projectName) {
        basedir = new File(super.getBasedir() + "/src/test/resources/unit/" + projectName);

        File pom = new File(getBasedir(), "pom.xml");
        try (InputStream in = new FileInputStream(pom)) {
            setModel(new MavenXpp3Reader().read(in));
            Site site = new Site();
            site.setId("localhost");
            distributionManagement.setSite(site);
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.apache.maven.project.MavenProject#getName()
     */
    @Override
    public String getName() {
        return getModel().getName();
    }

    /**
     * @see org.apache.maven.project.MavenProject#getProperties()
     */
    @Override
    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public DistributionManagement getDistributionManagement() {
        return distributionManagement;
    }

    /** {@inheritDoc} */
    @Override
    public File getBasedir() {
        return basedir;
    }

    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }
}
