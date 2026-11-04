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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MojoThreadExecutor} (MSITE-849).
 */
class MojoThreadExecutorTest {

    /**
     * Tasks submitted from other threads, as the HTTP server does, see the thread-bound state of the
     * thread running the loop, which is how Maven's execution scopes are looked up.
     */
    @Test
    void runsTasksOnTheLoopThread() throws Exception {
        ThreadLocal<String> scope = new ThreadLocal<>();
        MojoThreadExecutor executor = new MojoThreadExecutor();
        AtomicReference<String> seen = new AtomicReference<>();
        AtomicReference<Thread> ranOn = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        Thread submitter = new Thread(() -> {
            executor.execute(() -> {
                seen.set(scope.get());
                ranOn.set(Thread.currentThread());
                done.countDown();
            });
            try {
                assertTrue(done.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                executor.stop();
            }
        });

        scope.set("mojo execution scope");
        submitter.start();
        executor.runUntilStopped();
        submitter.join();

        assertEquals("mojo execution scope", seen.get());
        assertSame(Thread.currentThread(), ranOn.get());
    }

    @Test
    void failingTaskDoesNotStopTheLoop() throws Exception {
        MojoThreadExecutor executor = new MojoThreadExecutor();
        AtomicReference<Boolean> second = new AtomicReference<>(false);

        executor.execute(() -> {
            throw new IllegalStateException("boom");
        });
        executor.execute(() -> second.set(true));
        executor.stop();
        executor.runUntilStopped();

        assertTrue(second.get());
    }
}
