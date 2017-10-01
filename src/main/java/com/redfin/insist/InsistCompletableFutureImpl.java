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

import com.redfin.patience.PatientTimeoutException;
import com.redfin.patience.PatientWait;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

import static com.redfin.validity.Validity.validate;

/**
 * Implementation of the {@link InsistCompletableFuture} interface.
 *
 * @param <X> the type of Throwable to be thrown if validation doesn't succeed in time.
 */
final class InsistCompletableFutureImpl<X extends Throwable>
 implements InsistCompletableFuture<X> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String CUSTOM_FORMAT = "%s : Timeout reached after %d unsuccessful attempt(s)";
    private static final String DEFAULT_FORMAT = "Timeout reached after %d unsuccessful attempt(s)";

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Fields & Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final BiFunction<String, Throwable, X> throwableFunction;
    private final String message;
    private final PatientWait wait;

    private Duration timeout;

    /**
     * Create a new InsistCompletableFutureImpl instance with the given arguments.
     *
     * @param throwableFunction the function to take in a string and throwable and
     *                          create a new throwable.
     *                          May not be null.
     * @param message           the String message prefix if validation fails.
     * @param wait              the {@link com.redfin.patience.PatientWait} to use if waiting for validation to succeed.
     *                          May not be null.
     *
     * @throws IllegalArgumentException if throwableFunction or wait are null.
     */
    InsistCompletableFutureImpl(BiFunction<String, Throwable, X> throwableFunction,
                                String message,
                                PatientWait wait) {
        this.throwableFunction = validate().that(throwableFunction).isNotNull();
        this.message = message;
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
            // Failure, throw requested throwable type
            throw throwableFunction.apply(fail(message, exception.getAttemptsCount()), exception);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Throwable> T thatEventuallyThrows(Class<T> expectedThrowableClass,
                                                           Executable<T> executable) throws X {
        validate().that(expectedThrowableClass).isNotNull();
        validate().that(executable).isNotNull();
        T caught;
        try {
            caught = wait.from(() -> {
                try {
                    executable.execute();
                    // No throwable seen, return null
                    return null;
                } catch (Throwable thrown) {
                    // Throwable, check if it is the expected one
                    if (expectedThrowableClass.isAssignableFrom(thrown.getClass())) {
                        return (T) thrown;
                    } else {
                        return null;
                    }
                }
            }).get(timeout);
        } catch (PatientTimeoutException exception) {
            // Failure, throw requested throwable type
            throw throwableFunction.apply(fail(message, exception.getAttemptsCount()), exception);
        }
        return caught;
    }

    private static String fail(String message, int numAttempts) {
        if (null == message) {
            return String.format(DEFAULT_FORMAT, numAttempts);
        } else {
            return String.format(CUSTOM_FORMAT, message, numAttempts);
        }
    }
}
