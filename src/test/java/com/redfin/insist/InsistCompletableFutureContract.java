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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * A unit test contract for implementing sub-classes of the {@link InsistCompletableFuture} type.
 *
 * @param <X> the type of the throwable thrown by the sub class.
 */
interface InsistCompletableFutureContract<X extends Throwable>
  extends InsistFutureContract<X> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test contract requirements
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an instance of the insist completable future implementing class being tested.
     */
    InsistCompletableFuture<X> getInsistCompletableFutureInstance();

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    default void testInsistCompletableFutureWithinThrowsExceptionForNullDuration() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInsistCompletableFutureInstance().within(null),
                                "An InsistCompletableFuture should throw an exception for a null within duration.");
    }

    @Test
    default void testInsistCompletableFutureWithinThrowsExceptionForNegativeDuration() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInsistCompletableFutureInstance().within(Duration.ofSeconds(1).negated()),
                                "An InsistCompletableFuture should throw an exception for a negative within duration.");
    }

    @Test
    default void testInsistCompletableFutureWithinReturnsNonNullForValidDuration() {
        Assertions.assertNotNull(getInsistCompletableFutureInstance().within(Duration.ofSeconds(1)),
                                 "An InsistCompletableFuture should not return null for a valid within duration.");
    }

    @Test
    default void testInsistCompletableFutureWithinThrowsExceptionForNonTrueResultWithinTimeout() {
        Assertions.assertThrows(getThrowableClass(),
                                () -> getInsistCompletableFutureInstance().within(Duration.ofMillis(10))
                                                                          .thatEventually(() -> false),
                                "An InsistCompletableFuture should throw the given throwable if true isn't given within the timeout.");
    }

    @Test
    default void testInsistCompletableFutureWithinDoesPassWithTrueResultWithinTimeout() throws X {
        // Shouldn't throw
        getInsistCompletableFutureInstance().within(Duration.ofMillis(10))
                                            .thatEventually(() -> true);
    }
}
