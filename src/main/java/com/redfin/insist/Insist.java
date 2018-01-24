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

import com.redfin.insist.executor.AbortedFailedValidationExecutor;
import com.redfin.insist.executor.AssertionFailedValidationExecutor;
import com.redfin.insist.impl.InsistVerifiableFactory;
import com.redfin.validity.ValidityUtils;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

/**
 * A static class used as the entry point in using the Insist library.
 */
public final class Insist {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final AssertionFailedValidationExecutor ASSERT_EXECUTOR;
    private static final AbortedFailedValidationExecutor ASSUME_EXECUTOR;

    /*
     * The null message instances of the factories can be re-used safely. Cache them
     * for better performance. A custom message will require a new instance, though.
     */

    private static final InsistVerifiableFactory<AssertionFailedError> NULL_MESSAGE_ASSERT_FACTORY;
    private static final InsistVerifiableFactory<TestAbortedException> NULL_MESSAGE_ASSUME_FACTORY;

    static {
        ASSERT_EXECUTOR = new AssertionFailedValidationExecutor();
        ASSUME_EXECUTOR = new AbortedFailedValidationExecutor();
        NULL_MESSAGE_ASSERT_FACTORY = new InsistVerifiableFactory<>(() -> null, ASSERT_EXECUTOR);
        NULL_MESSAGE_ASSUME_FACTORY = new InsistVerifiableFactory<>(() -> null, ASSUME_EXECUTOR);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Static Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an {@link InsistVerifiableFactory} instance with the default message prefix thatEventually
     * throws an {@link AssertionFailedError} on validation failure.
     */
    public static InsistVerifiableFactory<AssertionFailedError> asserts() {
        return NULL_MESSAGE_ASSERT_FACTORY;
    }

    /**
     * @return an {@link InsistVerifiableFactory} instance with the default message prefix thatEventually
     * throws a {@link TestAbortedException} on validation failure.
     */
    public static InsistVerifiableFactory<TestAbortedException> assumes() {
        return NULL_MESSAGE_ASSUME_FACTORY;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
     * Ensure this class is not instantiable, even through reflection.
     */

    private Insist() {
        throw new AssertionError(ValidityUtils.nonInstantiableMessage());
    }
}
