
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

def content = new File( basedir, 'target/generated-site/processed/tools.apt' ).text

assert content.contains( 'configured=Hello tools.html' ) : 'custom SafeConfig tool must get its property and the context'
assert content.contains( 'setter=5' ) : 'custom tool must be configured through its setter'
assert content.contains( 'date=26/10/2026' ) : 'site-tools.xml must override the format of the date tool'
assert content.contains( 'render=&lt;b&gt;' ) : 'render.eval must see the other tools'
assert content.contains( 'esc=a&apos;b' )
assert content.contains( 'sorter=abc' )
assert content.contains( 'convert=true' )
assert content.contains( 'text=Version' )
assert content.contains( 'context=true' )
