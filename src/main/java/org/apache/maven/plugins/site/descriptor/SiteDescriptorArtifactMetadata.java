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
package org.apache.maven.plugins.site.descriptor;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.AbstractArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;
import org.apache.maven.doxia.site.SiteModel;
import org.apache.maven.doxia.site.io.xpp3.SiteXpp3Writer;
import org.codehaus.plexus.util.xml.XmlStreamWriter;

/**
 * Attach a POM to an artifact.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class SiteDescriptorArtifactMetadata extends AbstractArtifactMetadata {
    private final SiteModel siteModel;

    private final File file;

    public SiteDescriptorArtifactMetadata(Artifact artifact, SiteModel siteModel, File file) {
        super(artifact);

        this.file = file;
        this.siteModel = siteModel;
    }

    @Override
    public String getRemoteFilename() {
        return getFilename();
    }

    @Override
    public String getLocalFilename(ArtifactRepository repository) {
        return getFilename();
    }

    private String getFilename() {
        return getArtifactId() + "-" + artifact.getVersion() + "-" + file.getName();
    }

    @Override
    public void storeInLocalRepository(ArtifactRepository localRepository, ArtifactRepository remoteRepository)
            throws RepositoryMetadataStoreException {
        File destination = new File(
                localRepository.getBasedir(), localRepository.pathOfLocalRepositoryMetadata(this, remoteRepository));

        if (!destination.getParentFile().mkdirs()) {
            throw new RepositoryMetadataStoreException(
                    "Could not create artifact directory " + destination + " in local repository");
        }

        try (Writer writer = new XmlStreamWriter(destination)) {
            new SiteXpp3Writer().write(writer, siteModel);
        } catch (IOException e) {
            throw new RepositoryMetadataStoreException("Error saving in local repository", e);
        }
    }

    @Override
    public String toString() {
        return "site descriptor for " + artifact.getArtifactId() + " " + artifact.getVersion() + " " + file.getName();
    }

    @Override
    public boolean storedInArtifactVersionDirectory() {
        return true;
    }

    @Override
    public String getBaseVersion() {
        return artifact.getBaseVersion();
    }

    @Override
    public Object getKey() {
        return "site descriptor " + artifact.getGroupId() + ":" + artifact.getArtifactId() + " " + file.getName();
    }

    @Override
    public void merge(ArtifactMetadata metadata) {
        SiteDescriptorArtifactMetadata m = (SiteDescriptorArtifactMetadata) metadata;
        if (!m.file.equals(file)) {
            throw new IllegalStateException("Cannot add two different pieces of metadata for: " + getKey());
        }
    }

    @Override
    public void merge(org.apache.maven.repository.legacy.metadata.ArtifactMetadata metadata) {
        // FIXME what todo here ?
    }
}
