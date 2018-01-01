/*
 * Copyright: (c) 2016 Redfin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redfin.insist;

import com.redfin.patience.PatientExecutionHandlers;
import com.redfin.patience.PatientRetryHandlers;
import com.redfin.patience.PatientWait;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;

final class InsistCompletableFutureImplTest
 implements InsistCompletableFutureContract<AssertionFailedError> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test values & contract implementations
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final Duration DURATION = Duration.ofMillis(200);
    private static final PatientWait WAIT = PatientWait.builder()
                                                       .withInitialDelay(Duration.ZERO)
                                                       .withExecutionHandler(PatientExecutionHandlers.simple())
                                                       .withRetryHandler(PatientRetryHandlers.fixedDelay(Duration.ofMillis(10)))
                                                       .withDefaultTimeout(DURATION)
                                                       .build();

    @Override
    public Class<AssertionFailedError> getThrowableClass() {
        return AssertionFailedError.class;
    }

    @Override
    public InsistFuture<AssertionFailedError> getInsistFutureInstance() {
        return getInsistCompletableFutureInstance().within(DURATION);
    }

    @Override
    public InsistCompletableFuture<AssertionFailedError> getInsistCompletableFutureInstance() {
        return getInstance(null);
    }

    private InsistCompletableFuture<AssertionFailedError> getInstance(String message) {
        return Insist.asserts().withMessage(message).withWait(WAIT);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void testFailureHasExpectedDefaultMessage() {
        AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                                                              () -> getInstance(null).within(Duration.ZERO)
                                                                                     .thatEventually(() -> false),
                                                              "An AssertionFailedError should have been thrown.");
        Assertions.assertTrue(thrown.getMessage().startsWith("Timeout reached after 1 unsuccessful attempt(s)"),
                                "Should have thrown the expected message for a null message verification.");
    }

    @Test
    void testFailureHasExpectedGivenMessage() {
        String message = "hello";
        AssertionFailedError thrown = Assertions.assertThrows(AssertionFailedError.class,
                                                              () -> getInstance(message).within(Duration.ZERO)
                                                                                        .thatEventually(() -> false),
                                                              "An AssertionFailedError should have been thrown.");
        Assertions.assertTrue(thrown.getMessage()
                                    .startsWith(String.format("%s : %s",
                                                              message,
                                                              "Timeout reached after 1 unsuccessful attempt(s)")),
                              "Should have thrown the expected message for a given message verification.");
    }
}
