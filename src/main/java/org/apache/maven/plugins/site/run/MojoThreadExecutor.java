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
package org.apache.maven.plugins.site.run;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An {@link Executor} that runs every submitted task on the thread calling {@link #runUntilStopped()}.
 * <p>
 * {@code site:run} serves HTTP requests on its own threads and hands the rendering of documents to this
 * executor, which runs it on the Mojo thread. Maven's mojo execution scope ({@code MavenProject},
 * {@code MojoExecution}) is only available on that thread, and Doxia components contributed by site
 * dependencies, such as parsers, are instantiated lazily while a document is rendered.
 */
class MojoThreadExecutor implements Executor {
    private static final Runnable STOP = () -> {};

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    @Override
    public void execute(Runnable task) {
        tasks.add(task);
    }

    /**
     * Runs submitted tasks one at a time on the calling thread until {@link #stop()} is called. A task that
     * fails does not end the loop.
     *
     * @throws InterruptedException if the calling thread is interrupted while waiting for a task
     */
    void runUntilStopped() throws InterruptedException {
        for (Runnable task = tasks.take(); task != STOP; task = tasks.take()) {
            try {
                task.run();
            } catch (RuntimeException e) {
                // the task reports its own failure to the client; keep serving
            }
        }
    }

    /**
     * Ends {@link #runUntilStopped()} once the tasks submitted before this call have run.
     */
    void stop() {
        tasks.add(STOP);
    }
}
