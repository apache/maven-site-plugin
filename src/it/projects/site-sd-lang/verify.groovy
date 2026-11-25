
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

// TODO: also verify module links in generated files!

// SITE

File topLevelDirectory = new File(basedir, "target/site")
assert topLevelDirectory.exists() && topLevelDirectory.isDirectory() : "Site directory '" + topLevelDirectory + "' is missing or not a directory."

File moduleDirectory = new File( basedir, "mymodule/target/site" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "Site module directory '" + moduleDirectory + "' is missing or not a directory."

File indexFile = new File( topLevelDirectory, "index.html" )
assert indexFile.exists() && indexFile.isFile() : "Site index.html '" + indexFile + "' is missing or not a file."
String content = indexFile.getText('UTF-8')
assert content.contains( "the following locales are supported: [, en, fr, de, de_DE, de_AT, zh_TW]" ) : "index.html does not contain supported locales"

File projectInfoFile = new File( topLevelDirectory, "project-info.html" )
assert projectInfoFile.exists() && projectInfoFile.isFile() : "Site project-info.html '" + projectInfoFile + "' is missing or not a file."

File frDirectory = new File( topLevelDirectory, "fr" )
assert frDirectory.exists() && frDirectory.isDirectory() : "Site fr directory '" + frDirectory + "' is missing or not a directory."

File frProjectInfoFile = new File( frDirectory, "project-info.html" )
assert frProjectInfoFile.exists() && frProjectInfoFile.isFile() : "Site fr project-info.html '" + frProjectInfoFile + "' is missing or not a file."

File frModuleDirectory = new File( moduleDirectory, "fr" )
assert frModuleDirectory.exists() && frModuleDirectory.isDirectory() : "Site fr module directory '" + frModuleDirectory + "' is missing or not a directory."

File moduleProjectInfoFile = new File( moduleDirectory, "project-info.html" )
assert moduleProjectInfoFile.exists() && moduleProjectInfoFile.isFile() : "Site module project-info.html '" + moduleProjectInfoFile + "' is missing or not a file."

File frModuleProjectInfoFile = new File( frModuleDirectory, "project-info.html" )
assert frModuleProjectInfoFile.exists() && frModuleProjectInfoFile.isFile() : "Site fr module project-info.html '" + frModuleProjectInfoFile + "' is missing or not a file."

// STAGING

topLevelDirectory = new File( basedir, "target/staging" )
assert topLevelDirectory.exists() && topLevelDirectory.isDirectory() : "Staging directory '" + topLevelDirectory + "' is missing or not a directory."

moduleDirectory = new File( topLevelDirectory, "mymodule" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "Staging module directory '" + moduleDirectory + "' is missing or not a directory."

frDirectory = new File( topLevelDirectory, "fr" )
assert frDirectory.exists() && frDirectory.isDirectory() : "Staging fr directory '" + frDirectory + "' is missing or not a directory."

frModuleDirectory = new File( frDirectory, "mymodule" )
assert frModuleDirectory.exists() && frModuleDirectory.isDirectory() : "Staging fr module directory '" + frModuleDirectory + "' is missing or not a directory."

// DEPLOY

topLevelDirectory = new File( basedir, "deploy" )
assert topLevelDirectory.exists() && topLevelDirectory.isDirectory() : "Deploy directory '" + topLevelDirectory + "' is missing or not a directory."

moduleDirectory = new File( topLevelDirectory, "mymodule" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "Staging module directory '" + moduleDirectory + "' is missing or not a directory."

frDirectory = new File( topLevelDirectory, "fr" )
assert frDirectory.exists() && frDirectory.isDirectory() : "Staging fr directory '" + frDirectory + "' is missing or not a directory."

frModuleDirectory = new File( frDirectory, "mymodule" )
assert frModuleDirectory.exists() && frModuleDirectory.isDirectory() : "Staging fr module directory '" + frModuleDirectory + "' is missing or not a directory."

// STAGE DEPLOY

topLevelDirectory = new File( topLevelDirectory, "staging" )
assert topLevelDirectory.exists() && topLevelDirectory.isDirectory() : "Stage deploy directory '" + topLevelDirectory + "' is missing or not a directory."

moduleDirectory = new File( topLevelDirectory, "mymodule" )
assert moduleDirectory.exists() && moduleDirectory.isDirectory() : "Staging module directory '" + moduleDirectory + "' is missing or not a directory."

frDirectory = new File( topLevelDirectory, "fr" )
assert frDirectory.exists() && frDirectory.isDirectory() : "Staging fr directory '" + frDirectory + "' is missing or not a directory."

frModuleDirectory = new File( frDirectory, "mymodule" )
assert frModuleDirectory.exists() && frModuleDirectory.isDirectory() : "Staging fr module directory '" + frModuleDirectory + "' is missing or not a directory."
