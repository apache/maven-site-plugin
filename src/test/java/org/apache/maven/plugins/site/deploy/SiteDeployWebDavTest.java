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

import org.apache.maven.plugin.AbstractMojo;

/**
 * @author Olivier Lamy
 * @since 3.0-beta-2
 *
 */
public class SiteDeployWebDavTest extends AbstractSiteDeployWebDavTest {

    @Override
    String getMojoName() {
        return "deploy";
    }

    @Override
    AbstractMojo getMojo(File pomXmlFile) throws Exception {
        return (SiteDeployMojo) lookupMojo(getMojoName(), pomXmlFile);
    }
}
