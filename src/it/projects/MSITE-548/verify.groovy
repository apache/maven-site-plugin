
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

File deployDirectory = new File( basedir, "deploy" )
assert deployDirectory.exists() && deployDirectory.isDirectory() : "deploy directory is missing or not a directory:" + deployDirectory.getAbsolutePath()

File rootDirectory = new File( deployDirectory, "MSITE-548" )
assert rootDirectory.exists() && rootDirectory.isDirectory() : "rootDirectory file is missing or not a directory."

File junkDirectory = new File( rootDirectory, "junk" )
assert junkDirectory.exists() && junkDirectory.isDirectory() : "junkDirectory file is missing or not a directory."

File module1Directory = new File( deployDirectory, "module1" )
assert module1Directory.exists() && module1Directory.isDirectory() : "module1Directory file is missing or not a directory."

File module2Directory = new File( deployDirectory, "module-2" )
assert module2Directory.exists() && module2Directory.isDirectory() : "module2Directory file is missing or not a directory."
