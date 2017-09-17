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
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * A unit test contract for implementing sub-classes of the {@link InsistFuture} type.
 *
 * @param <X> the type of the throwable thrown by the sub class.
 */
interface InsistFutureContract<X extends Throwable> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test contract requirements
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return the class object of the throwable type X.
     */
    Class<X> getThrowableClass();

    /**
     * Note thatEventually the instance should have timing characteristics such thatEventually it will
     * execute the boolean supplier more than once.
     *
     * @return an instance of the insist future implementing class being tested.
     */
    InsistFuture<X> getInsistFutureInstance();

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // --------------------------------------------------------------
    // thatEventually tests
    // --------------------------------------------------------------

    @Test
    default void testThatEventuallyThrowsExceptionForNullBooleanSupplier() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInsistFutureInstance().thatEventually(null),
                                "InsistFuture thatEventually() should throw an exception for a null boolean supplier.");
    }

    @Test
    default void testThatEventuallyExecutesAgainWhenFalseValueIsGiven() throws X {
        final AtomicInteger integer = new AtomicInteger(0);
        BooleanSupplier supplier = () -> {
            switch (integer.incrementAndGet()) {
                case 2:
                    // Second attempt should return true
                    return true;
                default:
                    // Any other attempt should return false
                    return false;
            }
        };
        getInsistFutureInstance().thatEventually(supplier);
        Assertions.assertEquals(2,
                                integer.get(),
                                "InsistFuture thatEventually() should execute again if false is found from the supplier");
    }

    @Test
    default void testThatEventuallyStopsExecutingWhenTrueValueIsGiven() throws X {
        final AtomicInteger integer = new AtomicInteger(0);
        BooleanSupplier supplier = () -> {
            switch (integer.incrementAndGet()) {
                case 1:
                    // First attempt should return false
                    return false;
                case 2:
                    // Second attempt should return true
                    return true;
                default:
                    // Third or later attempts should throw an exception
                    throw new AssertionError("InsistFuture thatEventually() should stop executing after the supplier returns true.");
            }
        };
        getInsistFutureInstance().thatEventually(supplier);
    }

    @Test
    default void testThatEventuallyWillThrowIfTrueIsNeverReceived() {
        Assertions.assertThrows(getThrowableClass(),
                                () -> getInsistFutureInstance().thatEventually(() -> false),
                                "InsistFuture thatEventually() should throw an exception if true is never received from the supplier.");
    }

    // --------------------------------------------------------------
    // thatEventuallyThrows tests
    // --------------------------------------------------------------

    @Test
    default void testThatEventuallyThrowsThrowsExceptionForNullExpectedThrowable() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInsistFutureInstance().thatEventuallyThrows(null, () -> {}),
                                "InsistFuture thatEventuallyThrows() should throw an exception if a null expected throwable class is given.");
    }

    @Test
    default void testThatEventuallyThrowsThrowsExceptionForNullExecutable() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInsistFutureInstance().thatEventuallyThrows(IllegalArgumentException.class, null),
                                "InsistFuture thatEventuallyThrows() should throw an exception if a null executable is given.");
    }

    @Test
    default void testThatEventuallyThrowsThrowsExceptionIfExpectedThrowableIsNotThrown() {
        Assertions.assertThrows(AssertionFailedError.class,
                                () -> getInsistFutureInstance().thatEventuallyThrows(IllegalArgumentException.class,
                                                                                     () -> {}),
                                "InsistFuture thatEventuallyThrows() should throw an exception if the expected throwable isn't seen.");
    }

    @Test
    default void testThatEventuallyThrowsIgnoresOtherThrowableTypes() {
        Assertions.assertThrows(AssertionFailedError.class,
                                () -> getInsistFutureInstance().thatEventuallyThrows(IllegalArgumentException.class,
                                                                                     () -> { throw new IllegalStateException("whoops"); }),
                                "InsistFuture thatEventuallyThrows() should throw an exception if the expected throwable isn't seen.");
    }

    @Test
    default void testThatEventuallyThrowsReturnsExpectedThrowableIfThrown() throws X {
        String message = "whoops";
        Assertions.assertEquals(message,
                                getInsistFutureInstance().thatEventuallyThrows(IllegalArgumentException.class,
                                                                               () -> { throw new IllegalArgumentException("whoops"); })
                                                         .getMessage(),
                                "InsistFuture thatEventuallyThrows should return the thrown expected exception.");
    }
}
