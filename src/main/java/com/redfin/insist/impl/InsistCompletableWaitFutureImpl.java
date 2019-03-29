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

import com.redfin.insist.InsistCompletableWaitFuture;
import com.redfin.insist.InsistExecutable;
import com.redfin.insist.InsistFuture;
import com.redfin.patience.PatientWait;
import com.redfin.patience.exceptions.PatientTimeoutException;
import com.redfin.validity.FailedValidationExecutor;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.redfin.validity.Validity.validate;

/**
 * Implementation of the {@link InsistCompletableWaitFuture} interface that uses
 * the {@link PatientWait} class to wait for conditions.
 *
 * @param <X> the type of Throwable to be thrown if validation doesn't succeed in time.
 */
final class InsistCompletableWaitFutureImpl<X extends Throwable>
    extends AbstractFutureImpl<X>
 implements InsistCompletableWaitFuture<X> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String CUSTOM_FORMAT = "%s : Timeout reached after %d unsuccessful attempt(s)";
    private static final String DEFAULT_FORMAT = "Timeout reached after %d unsuccessful attempt(s)";

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Fields & Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final Supplier<String> messageSupplier;
    private final FailedValidationExecutor<X> failedValidationExecutor;
    private final PatientWait wait;

    private Duration timeout;

    /**
     * Create a new {@link InsistCompletableWaitFutureImpl} instance with the given arguments.
     *
     * @param messageSupplier          the {@link Supplier} of the String message prefix if validation fails.
     *                                 May not be null.
     * @param failedValidationExecutor the {@link FailedValidationExecutor} to be used if the attempt fails.
     *                                 May not be null.
     * @param wait                     the {@link PatientWait} to use while waiting for validation to succeed.
     *                                 May not be null.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    InsistCompletableWaitFutureImpl(Supplier<String> messageSupplier,
                                    FailedValidationExecutor<X> failedValidationExecutor,
                                    PatientWait wait) {
        this.messageSupplier = validate().that(messageSupplier).isNotNull();
        this.failedValidationExecutor = validate().that(failedValidationExecutor).isNotNull();
        this.wait = validate().that(wait).isNotNull();
        this.timeout = wait.getDefaultTimeout();
    }

    @Override
    public InsistFuture<X> within(Duration timeout) {
        this.timeout = validate().that(timeout).isGreaterThanOrEqualTo(Duration.ZERO);
        return this;
    }

    @Override
    public void thatEventually(BooleanSupplier supplier) throws X {
        validate().that(supplier).isNotNull();
        try {
            // The default filter considers false an invalid result so
            // this will always either find true or throw a timeout exception
            wait.from(supplier::getAsBoolean)
                .get(timeout);
        } catch (PatientTimeoutException exception) {
            // Failure
            failedValidationExecutor.fail("Eventually true",
                                          "always false",
                                          fail(messageSupplier, exception.getFailedAttemptsCount()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T thatEventuallyThrows(Class<T> expectedThrowableClass,
                                                        InsistExecutable<T> executable) throws X {
        validate().that(expectedThrowableClass).isNotNull();
        validate().that(executable).isNotNull();
        T caught = null;
        try {
            caught = wait.from(getEventuallyThrowsExecutable(expectedThrowableClass, executable))
                         .get(timeout);
        } catch (PatientTimeoutException exception) {
            // Failure
            failedValidationExecutor.fail("Expected to catch throwable '" + expectedThrowableClass.getName() + "'",
                                          "not caught",
                                          fail(messageSupplier, exception.getFailedAttemptsCount()));
        }
        return caught;
    }

    private static Supplier<String> fail(Supplier<String> messageSupplier, int numAttempts) {
        String message = messageSupplier.get();
        if (null == message) {
            return () -> String.format(DEFAULT_FORMAT, numAttempts);
        } else {
            return () -> String.format(CUSTOM_FORMAT, message, numAttempts);
        }
    }
}
