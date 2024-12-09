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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.FileSet;
import org.codehaus.plexus.archiver.util.DefaultFileSet;

/**
 * Adds the unprocessed (restricted) site content compressed in a ZIP archive as dedicated artifact with classifier {@value #CLASSIFIER} to be installed/deployed.
 * Usually used in combination with {@link org.apache.maven.plugins.site.descriptor.SiteDescriptorAttachMojo} to also deploy the actual site descriptor.
 * Any inheriting site will implicitly have this site content (but each file may be overwritten).
 *
 * @since next
 */
@Mojo(name = SiteResourcesAttachMojo.GOAL_NAME, defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true)
public class SiteResourcesAttachMojo extends AbstractSiteMojo {

    public static final String GOAL_NAME = "attach-resources";

    /**
     * Directory containing the <code>site.xml</code> file and the source for hand written docs (one directory
     * per Doxia-source-supported markup types)
     * @see <a href="/doxia/references/index.html">Doxia Markup Languages References</a>.
     */
    @Parameter(defaultValue = "${basedir}/src/site")
    protected File siteDirectory;

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    @Component(hint = ARCHIVE_EXTENSION)
    private Archiver zipArchiver;

    /**
     * The file name patterns to exclude (potentially in addition to the default ones mentioned at {@link #addDefaultExcludes}).
     * The format of each pattern is described in {@link org.codehaus.plexus.util.DirectoryScanner}.
     * The comparison is performed against the file path relative to the {@link #siteDirectory}.
     * <p>
     * Each value is either a regex pattern if enclosed within {@code %regex[} and {@code ]}, otherwise an
     * <a href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant pattern</a>.
     * <p>
     * Exclusions take precedence over inclusions via {@link #includes}.
     * <p>
     * In any case all site descriptors are always excluded ({@code site.xml} and {@code site_*.xml}).
     */
    @Parameter(defaultValue = "**/.gitattributes", required = true)
    protected Set<String> excludes;

    /**
     * The file name patterns to include. The format of each pattern is described in {@link org.codehaus.plexus.util.DirectoryScanner}.
     * The comparison is performed against the file path relative to the {@link #siteDirectory}.
     * <p>
     * Each value is either a regex pattern if enclosed within {@code %regex[} and {@code ]}, otherwise an
     * <a href="https://ant.apache.org/manual/dirtasks.html#patterns">Ant pattern</a>.
     * If this is not set, everything is included.
     * <p>
     * Exclusions via {@link #excludes} take precedence over inclusions.
     */
    @Parameter(required = false)
    protected Set<String> includes;

    /**
     * By default certain metadata files are excluded which means they will not be copied into the package.
     * If you need them for a particular reason you can do that by setting this parameter to {@code false}.
     *
     * @see org.codehaus.plexus.util.AbstractScanner#DEFAULTEXCLUDES
     */
    @Parameter(defaultValue = "true")
    protected boolean addDefaultExcludes;

    /**
     * If set to {@code true} attaches site resources only if packaging is pom. For all other packagings this mojo is skipped.
     * If set to {@code false} site resources are attached for all packagings.
     */
    @Parameter(defaultValue = "true")
    private boolean pomPackagingOnly;

    public static final String CLASSIFIER = "site-resources";
    public static final String ARCHIVE_EXTENSION = "zip";

    public void execute() throws MojoExecutionException {
        if (pomPackagingOnly && !"pom".equals(project.getPackaging())) {
            // https://issues.apache.org/jira/browse/MSITE-597
            getLog().info("Skipping because packaging '" + project.getPackaging() + "' is not pom.");
            return;
        }

        if (siteDirectory.exists()) {
            try {
                File destFile = new File(
                        project.getBuild().getDirectory(),
                        project.getBuild().getFinalName() + "-" + CLASSIFIER + "." + ARCHIVE_EXTENSION);

                File siteResourcesArchiveFile = createArchive(zipArchiver, destFile);
                // Attach the site resources archive
                getLog().info("Attaching site resources archive with classifier '" + CLASSIFIER + "'.");
                projectHelper.attachArtifact(project, ARCHIVE_EXTENSION, CLASSIFIER, siteResourcesArchiveFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to archive site resources", e);
            }
        } else {
            getLog().warn("No site resources found: nothing to attach.");
        }
    }

    private FileSet createFileSet() {
        DefaultFileSet fileSet = new DefaultFileSet();
        fileSet.setDirectory(siteDirectory);
        Set<String> excludes = new HashSet<>(this.excludes);
        // always exclude all site descriptors
        excludes.add("site.xml");
        excludes.add("site_*.xml");
        fileSet.setExcludes(excludes.toArray(new String[0]));
        if (includes != null && !includes.isEmpty()) {
            fileSet.setIncludes(includes.toArray(new String[0]));
        }
        fileSet.setUsingDefaultExcludes(addDefaultExcludes);
        return fileSet;
    }

    public File createArchive(Archiver archiver, File destFile) throws ArchiverException, IOException {
        archiver.setDestFile(destFile);
        archiver.addFileSet(createFileSet());
        archiver.createArchive();
        return archiver.getDestFile();
    }

    public File getSiteDirectory() {
        return siteDirectory;
    }
}
