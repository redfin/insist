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

package com.redfin.insist.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AbstractFutureTest<T extends AbstractFutureImpl<AssertionFailedError>> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test constants, requirements, and helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an instance of the class under test.
     */
    private T getInstance() {
        return getInstance("hello");
    }

    /**
     * @param message the custom message to add to the exception on unsuccessful
     *                validation.
     *
     * @return an instance of the class under test.
     */
    abstract T getInstance(String message);

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Nested
    @DisplayName("when thatEventually(BooleanSupplier) is called")
    final class ThatEventuallyTests {

        @Test
        @DisplayName("throws an exception for a null supplier")
        void testThrowsForNullSupplier() {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance().thatEventually(null),
                                    "Should throw an exception for a null boolean supplier.");
        }

        @Test
        @DisplayName("stops executing once true is returned")
        void testStopsExecutingOnceTrueIsReturned() {
            AtomicInteger counter = new AtomicInteger(0);
            getInstance().thatEventually(() -> counter.incrementAndGet() == 2);
            Assertions.assertEquals(2,
                                    counter.get(),
                                    "Should keep retrying the supplier until true is found.");
        }

        @Test
        @DisplayName("throws expected exception if true is never returned")
        void testThrowsForNoTrueResult() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance().thatEventually(() -> false),
                                    "Should throw for a supplier that never returns true.");
        }

        @Test
        @DisplayName("includes the custom message in the thrown exception")
        void testCustomMessageIncluded() {
            String message = "customMessage";
            Assertions.assertTrue(Assertions.assertThrows(AssertionFailedError.class,
                                                          () -> getInstance(message).thatEventually(() -> false),
                                                          "Should throw for a supplier that never returns true.")
                                            .getMessage()
                                            .contains(message),
                                  "The thrown exception should contain the given custom message");
        }

        @Test
        @DisplayName("throws as expected even if the message supplier returns null")
        void testNullMessageDoesNotCauseError() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance(null).thatEventually(() -> false),
                                    "Should throw for a supplier that never returns true.");
        }
    }

    @Nested
    @DisplayName("when thatEventuallyIsPresent(Supplier) is called")
    final class ThatEventuallyIsPresentTests {

        @Test
        @DisplayName("throws an exception for a null supplier")
        void testThrowsExceptionForNullSupplier() {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance(null).thatEventuallyIsPresent(null),
                                    "Should throw for a null supplier.");
        }

        @Test
        @DisplayName("throws the expected exception for a null optional")
        void testThrowsExpectedExceptionForNullOptional() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance(null).thatEventuallyIsPresent(() -> null),
                                    "Should throw the expected assertion failure for a null optional from the supplier.");
        }

        @Test
        @DisplayName("throws the expected exception for a never present optional")
        void testThrowsExceptionForNeverPresentOptional() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance(null).thatEventuallyIsPresent(Optional::empty),
                                    "Should throw the expected assertion failure for a never present optional.");
        }

        @Test
        @DisplayName("returns without throwing for an eventually present optional")
        void testReturnsWithoutThrowingForPresentOptional() {
            AtomicInteger counter = new AtomicInteger(0);
            try {
                getInstance(null).thatEventuallyIsPresent(() -> {
                    if (counter.getAndIncrement() == 2) {
                        return Optional.of("hello");
                    } else {
                        return Optional.empty();
                    }
                });
            } catch (AssertionError ae) {
                Assertions.fail("Should not have thrown an assertion for an optional that eventually was present");
            }
        }
    }

    @Nested
    @DisplayName("when thatEventuallyIsNotNull(Supplier) is called")
    final class ThatEventuallyIsNotNullTests {

        @Test
        @DisplayName("throws an exception for a null supplier")
        void testThrowsExceptionForNullSupplier() {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance(null).thatEventuallyIsNotNull(null),
                                    "Should throw for a null supplier.");
        }

        @Test
        @DisplayName("throws the expected exception for a never non-null value")
        void testThrowsExceptionForNeverNonNullValue() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance(null).thatEventuallyIsNotNull(() -> null),
                                    "Should throw the expected assertion for an always null value.");
        }

        @Test
        @DisplayName("returns without throwing for an eventually non-null value")
        void testReturnsWithoutThrowingForEventuallyNonNullValue() {
            AtomicInteger counter = new AtomicInteger(0);
            try {
                getInstance(null).thatEventuallyIsNotNull(() -> {
                    if (counter.getAndIncrement() == 2) {
                        return "hello";
                    } else {
                        return null;
                    }
                });
            } catch (AssertionError ae) {
                Assertions.fail("Should not have thrown an assertion for an eventually non-null value.");
            }
        }
    }

    @Nested
    @DisplayName("when thatEventuallyThrows(Class, InsistExecutable) is called")
    final class ThatEventuallyThrowsTests {

        @Test
        @DisplayName("throws an exception for a null expected throwable class")
        void testThrowsForNullExpectedThrowableClass() {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance().thatEventuallyThrows(null, () -> { }),
                                    "Should throw an exception for a null boolean supplier.");
        }

        @Test
        @DisplayName("throws an exception for a null executable")
        void testThrowsForNullExecutable() {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance().thatEventuallyThrows(NullPointerException.class, null),
                                    "Should throw an exception for a null boolean supplier.");
        }

        @Test
        @DisplayName("stops executing once expected throwable is thrown")
        void testStopsExecutingOnceExpectedThrowableIsThrown() {
            AtomicInteger counter = new AtomicInteger(0);
            getInstance().thatEventuallyThrows(NullPointerException.class,
                                               () -> {
                                                   if (counter.incrementAndGet() == 2) {
                                                       throw new NullPointerException();
                                                   }
                                               });
            Assertions.assertEquals(2,
                                    counter.get(),
                                    "Should keep retrying the supplier until the expected throwable is found.");
        }

        @Test
        @DisplayName("stops executing once subclass of expected throwable type is thrown")
        void testStopsExecutingOnceSubclassOfExpectedThrowableIsThrown() {
            getInstance().thatEventuallyThrows(RuntimeException.class,
                                               () -> { throw new NullPointerException(); });
        }

        @Test
        @DisplayName("throws expected exception if expected throwable is never thrown")
        void testThrowsForNoExpectedThrowableThrown() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance().thatEventuallyThrows(NullPointerException.class, () -> { }),
                                    "Should throw for a supplier that never returns true.");
        }

        @Test
        @DisplayName("throws expected exception if unexpected throwable is thrown")
        void testThrowsForUnExpectedThrowableThrown() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance().thatEventuallyThrows(NullPointerException.class, () -> { throw new AssertionError(); }),
                                    "Should throw for a supplier that never returns true.");
        }

        @Test
        @DisplayName("includes the custom message in the thrown exception")
        void testCustomMessageIncluded() {
            String message = "customMessage";
            Assertions.assertTrue(Assertions.assertThrows(AssertionFailedError.class,
                                                          () -> getInstance(message).thatEventuallyThrows(NullPointerException.class, () -> { }),
                                                          "Should throw for a supplier that never returns true.")
                                            .getMessage()
                                            .contains(message),
                                  "The thrown exception should contain the given custom message");
        }

        @Test
        @DisplayName("throws as expected even if the message supplier returns null")
        void testNullMessageDoesNotCauseError() {
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance(null).thatEventuallyThrows(NullPointerException.class, () -> { }),
                                    "Should throw for a supplier that never returns true.");
        }
    }
}
