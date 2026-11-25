
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

final File deployDirectory = new File(basedir, "deployed")
final File stageDirectory = new File(basedir, "target/staging")
final File stageDeployDirectory = new File(deployDirectory, "staging")

// top

File siteDir = new File(basedir, "target/site")
assert siteDir.exists() && siteDir.isDirectory() : "top site is missing or not a directory."

File stageDir = new File( stageDirectory, "." )
assert stageDir.exists() && stageDir.isDirectory() : "top stage site is missing or not a directory."

File deployDir = new File( deployDirectory, "." )
assert deployDir.exists() && deployDir.isDirectory() : "top deploy site is missing or not a directory."

File stageDeployDir = new File( stageDeployDirectory, "." )
assert stageDeployDir.exists() && stageDeployDir.isDirectory() : "top stage-deploy site is missing or not a directory."

// site

siteDir = new File( basedir, "site/target/site" )
assert siteDir.exists() && siteDir.isDirectory() : "site site is missing or not a directory."

stageDir = new File( stageDirectory, "site" )
assert stageDir.exists() && stageDir.isDirectory() : "site stage site is missing or not a directory."

deployDir = new File( deployDirectory, "site" )
assert deployDir.exists() && deployDir.isDirectory() : "site deploy site is missing or not a directory."

stageDeployDir = new File( stageDeployDirectory, "site" )
assert stageDeployDir.exists() && stageDeployDir.isDirectory() : "site stage-deploy site is missing or not a directory."

// skip-site

siteDir = new File( basedir, "skip-site/target/site" )
assert !siteDir.exists() : "skip-site site exists."

stageDir = new File( stageDirectory, "skip-site" )
assert !stageDir.exists() : "skip-site stage site exists."

deployDir = new File( deployDirectory, "skip-site" )
assert !deployDir.exists() : "skip-site deploy site exists."

stageDeployDir = new File( stageDeployDirectory, "skip-site" )
assert !stageDeployDir.exists() : "skip-site stage-deploy site exists."

// skip-site-deploy

siteDir = new File( basedir, "skip-site-deploy/target/site" )
assert siteDir.exists() && siteDir.isDirectory() : "skip-site-deploy site is missing or not a directory."

stageDir = new File( stageDirectory, "skip-site-deploy" )
assert stageDir.exists() : "skip-site-deploy stage site should still exist."

deployDir = new File( deployDirectory, "skip-site-deploy" )
assert !deployDir.exists() : "skip-site-deploy deploy site exists."

stageDeployDir = new File( stageDeployDirectory, "skip-site-deploy" )
assert !stageDeployDir.exists() : "skip-site-deploy stage-deploy site exists."
