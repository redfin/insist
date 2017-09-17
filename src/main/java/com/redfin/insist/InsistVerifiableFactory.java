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

import com.redfin.patience.PatientWait;
import com.redfin.validity.AbstractVerifiableFactory;
import com.redfin.validity.FailedValidationExecutor;

import java.util.Objects;
import java.util.function.BiFunction;

import static com.redfin.validity.Validity.validate;

/**
 * The {@link AbstractVerifiableFactory} implementation thatEventually allows for
 * creating the verifiable instances for the insist library and allows for
 * patiently waiting for a successful state.
 *
 * @param <X> the Throwable type to be thrown on unsuccessful validation.
 */
public final class InsistVerifiableFactory<X extends Throwable>
           extends AbstractVerifiableFactory<X, InsistVerifiableFactory<X>> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Fields & Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final BiFunction<String, Throwable, X> throwableFunction;

    /**
     * Create a new InsistVerifiableFactory instance with the given arguments.
     *
     * @param message                  the String message prefix for use upon validation failure.
     * @param throwableFunction        the function thatEventually takes in a message and cause and creates
     *                                 a throwable for failed validation.
     *                                 May not be null.
     * @param failedValidationExecutor the failed validation executor to use upon validation
     *                                 failure.
     *                                 May not be null.
     *
     * @throws NullPointerException if throwableFunction or failedValidationExecutor are null.
     */
    InsistVerifiableFactory(String message,
                            BiFunction<String, Throwable, X> throwableFunction,
                            FailedValidationExecutor<X> failedValidationExecutor) {
        super(message, failedValidationExecutor);
        this.throwableFunction = Objects.requireNonNull(throwableFunction);
    }

    @Override
    protected InsistVerifiableFactory<X> getFactory(String message,
                                                    FailedValidationExecutor<X> failedValidationExecutor) {
        return new InsistVerifiableFactory<>(message,
                                             throwableFunction,
                                             failedValidationExecutor);
    }

    /**
     * @param wait the {@link PatientWait} object to be used to wait for successful validation.
     *
     * @return an InsistCompletableFuture instance initialized with the given wait object.
     *
     * @throws IllegalArgumentException if wait is null.
     */
    public InsistCompletableFuture<X> withWait(PatientWait wait) {
        validate().that(wait).isNotNull();
        return new InsistCompletableFutureImpl<>(throwableFunction, getMessage(), wait);
    }

    /**
     * Call the executable, If the executable throws a throwable of type T,
     * then exit normally. If a different type of throwable is thrown or no
     * throwable is thrown at all, then throw a throwable of type X.
     *
     * @param expectedThrowableClass the class of throwable that is expected.
     *                               May not be null.
     * @param executable             the executable to be called.
     *                               May not be null.
     * @param <T>                    the type of expectedThrowableClass.
     *
     * @return the throwable that was expected.
     *
     * @throws X                        the type to be thrown if expectedThrowableClass is never encountered.
     * @throws IllegalArgumentException if expectedThrowableClass or executable are null.
     */
    public <T extends Throwable> T thatThrows(Class<T> expectedThrowableClass,
                                              Executable<T> executable) throws X {
        validate().that(expectedThrowableClass).isNotNull();
        validate().that(executable).isNotNull();
        return withWait(PatientWait.builder().build()).thatEventuallyThrows(expectedThrowableClass, executable);
    }
}
