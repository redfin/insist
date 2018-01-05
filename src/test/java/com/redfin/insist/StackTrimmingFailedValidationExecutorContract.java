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

/**
 * A unit test contract for implementing sub-classes of the {@link AbstractStackTrimmingFailedValidationExecutor} type.
 *
 * @param <X> the type of the throwable thrown by the sub class.
 * @param <F> the type of the stack trimming failed validation executor to be tested.
 */
interface StackTrimmingFailedValidationExecutorContract<X extends Throwable,
                                                        F extends AbstractStackTrimmingFailedValidationExecutor<X>>
  extends FailedValidationExecutorContract<X> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test contract requirements
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an instance of the type under test.
     */
    F getStackTrimmingFailedValidationExecutor();

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    default void testCanInstantiate() {
        Assertions.assertNotNull(getStackTrimmingFailedValidationExecutor(),
                                 "Should be able to instantiate an instance of the type under test.");
    }

    @Test
    default void testReturnsNonNullDefaultMessage() {
        Assertions.assertNotNull(getStackTrimmingFailedValidationExecutor().getDefaultMessage(),
                                 "Should return a non-null default message.");
    }

    @Test
    default void testBuildThrowableReturnsNonNullForValidValues() {
        Assertions.assertNotNull(getStackTrimmingFailedValidationExecutor().buildThrowable("t -> null != t",
                                                                                           "hello",
                                                                                           "message time"),
                                 "Failed validation executor should return a non-null throwable for valid values to buildThrowable.");
    }

    @Test
    default void testBuildThrowableReturnsNonNullForNullActual() {
        Assertions.assertNotNull(getStackTrimmingFailedValidationExecutor().buildThrowable("t -> null != t",
                                                                                           null,
                                                                                           "message time"),
                                 "Failed validation executor should return a non-null throwable for valid values to buildThrowable.");
    }

    @Test
    default void testBuildThrowableReturnsNonNullForNullMessage() {
        Assertions.assertNotNull(getStackTrimmingFailedValidationExecutor().buildThrowable("t -> null != t",
                                                                                           "hello",
                                                                                           null),
                                 "Failed validation executor should return a non-null throwable for valid values to buildThrowable.");
    }

    @Test
    default void testBuildThrowableThrowsForNullExpected() {
        Assertions.assertThrows(NullPointerException.class,
                                () -> getStackTrimmingFailedValidationExecutor().buildThrowable(null,
                                                                                                "hello",
                                                                                                "message time"),
                                 "Failed validation executor should throw for a non expected to buildThrowable.");
    }
}
