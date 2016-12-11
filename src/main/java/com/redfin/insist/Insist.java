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

import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A static class used as the entry point in using the Insist library.
 * <p>
 * To make an assertion, use either the {@link #assertion()} or
 * {@link #assertion(String)} methods.
 * <p>
 * To make an assertion, use either the {@link #assumption()} or
 * {@link #assumption(String)} methods.
 */
public final class Insist {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final BiFunction<String, Throwable, AssertionFailedError> ASSERT_BI_FUNCTION = AssertionFailedError::new;
    private static final BiFunction<String, Throwable, TestAbortedException> ASSUME_BI_FUNCTION = TestAbortedException::new;
    private static final StackTrimmingFailedValidationExecutor<AssertionFailedError> ASSERT_EXECUTOR = new StackTrimmingFailedValidationExecutor<>(AssertionFailedError::new);
    private static final StackTrimmingFailedValidationExecutor<TestAbortedException> ASSUME_EXECUTOR = new StackTrimmingFailedValidationExecutor<>(TestAbortedException::new);

    /*
     * The null message instances of the factories can be re-used safely. Cache them
     * for better performance. A custom message will require a new instance, though.
     */

    private static final InsistVerifiableFactory<AssertionFailedError> ASSERT_FACTORY = new InsistVerifiableFactory<>(null, ASSERT_BI_FUNCTION, ASSERT_EXECUTOR);
    private static final InsistVerifiableFactory<TestAbortedException> ASSUME_FACTORY = new InsistVerifiableFactory<>(null, ASSUME_BI_FUNCTION, ASSUME_EXECUTOR);

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Static Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an {@link InsistVerifiableFactory} instance with the default message prefix that
     * throws an {@link AssertionFailedError} on validation failure.
     */
    public static InsistVerifiableFactory<AssertionFailedError> assertion() {
        return assertion(null);
    }

    /**
     * @param message the String message prefix used on assertion failure.
     *                If null, the default message prefix will be used.
     *
     * @return an {@link InsistVerifiableFactory} instance with the given message prefix that
     * throws an {@link AssertionFailedError} on validation failure.
     */
    public static InsistVerifiableFactory<AssertionFailedError> assertion(String message) {
        if (null == message) {
            return ASSERT_FACTORY;
        }
        return new InsistVerifiableFactory<>(message, ASSERT_BI_FUNCTION, ASSERT_EXECUTOR);
    }

    /**
     * @return an {@link InsistVerifiableFactory} instance with the default message prefix that
     * throws a {@link TestAbortedException} on validation failure.
     */
    public static InsistVerifiableFactory<TestAbortedException> assumption() {
        return assumption(null);
    }

    /**
     * @param message the String message prefix used on assumption failure.
     *                If null, the default message prefix will be used.
     *
     * @return an {@link InsistVerifiableFactory} instance with the given message prefix that
     * throws a {@link TestAbortedException} on validation failure.
     */
    public static InsistVerifiableFactory<TestAbortedException> assumption(String message) {
        if (null == message) {
            return ASSUME_FACTORY;
        }
        return new InsistVerifiableFactory<>(message, ASSUME_BI_FUNCTION, ASSUME_EXECUTOR);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
     * Ensure this class is not instantiable, even through reflection.
     */

    private Insist() {
        throw new AssertionError("Cannot instantiate the static class Insist");
    }
}
