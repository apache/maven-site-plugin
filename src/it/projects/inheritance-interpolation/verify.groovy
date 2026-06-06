
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

// POM inheritance+interpolation
content = new File( basedir, 'child/target/generated-site/processed/pom.apt' ).text;

// normal field check
assert content.contains( "<<<project.artifactId = 'repo-parent'>>>" );
assert content.contains( "<<<project.artifactId = 'relative-parent'>>>" );
assert content.contains( "<<<project.artifactId = 'reactor-parent'>>>" );
assert content.contains( "<<<project.artifactId = 'child'>>>" );

// url check (with its inheritance)
assert content.contains( '<<<http://maven.apache.org/$\\{property}>>>' ); // repo-parent
assert content.contains( '<<<http://maven.apache.org/$\\{property}/relative-parent>>>' ); // relative-parent
assert content.contains( '<<<http://maven.apache.org/prop-value-from-reactor-parent/relative-parent/reactor-parent>>>' ); // reactor-parent
assert content.contains( '<<<http://maven.apache.org/prop-value-from-child/relative-parent/reactor-parent/child>>>' ); // child

// site.xml Site Model inheritance+interpolation
effectiveSiteContent = new File( basedir, 'child/target/effective-site.xml' ).text;
childContent = new File( basedir, 'child/target/site/index.html' ).text;

// bannerLeft name
assert effectiveSiteContent.contains( '<bannerLeft name="bannerLeft project.artifactId = child" />' );
assert childContent.contains( 'bannerLeft project.artifactId = child' );

// breadcrumbs
assert childContent.contains( '<a href="../../index.html"' );
assert childContent.contains( '<a href="../index.html"' );
assert childContent.contains( '<a href="index.html"' );

reactorEffectiveSiteContent = new File( basedir, 'reactor-parent/target/effective-site.xml' ).text;
reactorContent = new File( basedir, 'reactor-parent/target/site/index.html' ).text;

// bannerLeft name
assert reactorEffectiveSiteContent.contains( '<bannerLeft name="bannerLeft project.artifactId = reactor-parent" />' );
assert reactorContent.contains( 'bannerLeft project.artifactId = reactor-parent' );
