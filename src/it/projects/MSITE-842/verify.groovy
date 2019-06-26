
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

File resultFile;
File expectedFile;

// Check MSITE-842.html (must exist and be exactly like the model
resultFile = new File(basedir, "target/site/MSITE-842.html");
expectedFile = new File(basedir, "src/it/MSITE-842.html");

assert resultFile.exists() && resultFile.isFile()
assert resultFile.text.normalize().equals(expectedFile.text.normalize())

// Check another-page.html (must exist and be exactly like the model
resultFile = new File(basedir, "target/site/another-page.html");
expectedFile = new File(basedir, "src/it/another-page.html");

assert resultFile.exists() && resultFile.isFile()
assert resultFile.text.normalize().equals(expectedFile.text.normalize())

// Check sub/sub.html (must exist and be exactly like the model
resultFile = new File(basedir, "target/site/sub/sub.html");
expectedFile = new File(basedir, "src/it/sub/sub.html");

assert resultFile.exists() && resultFile.isFile()
assert resultFile.text.normalize().equals(expectedFile.text.normalize())

