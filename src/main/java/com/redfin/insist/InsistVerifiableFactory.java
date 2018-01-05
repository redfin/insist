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
import com.redfin.validity.AbstractVerifiableFactory;
import com.redfin.validity.FailedValidationExecutor;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

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
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final PatientWait DEFAULT_WAIT = PatientWait.builder()
                                                               .withInitialDelay(Duration.ZERO)
                                                               .withDefaultTimeout(Duration.ZERO)
                                                               .withExecutionHandler(PatientExecutionHandlers.ignoringAll())
                                                               .withRetryHandler(PatientRetryHandlers.fixedDelay(Duration.ofMillis(500)))
                                                               .build();

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Fields & Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final FailedValidationExecutor<X> failedValidationExecutor;

    /**
     * Create a new InsistVerifiableFactory instance with the given arguments.
     *
     * @param messageSupplier          the {@link Supplier} of the String message prefix for use upon validation failure.
     *                                 May not be null.
     * @param failedValidationExecutor the failed validation executor to use upon validation
     *                                 failure.
     *                                 May not be null.
     *
     * @throws NullPointerException if throwableFunction or failedValidationExecutor are null.
     */
    InsistVerifiableFactory(Supplier<String> messageSupplier,
                            FailedValidationExecutor<X> failedValidationExecutor) {
        super(messageSupplier, failedValidationExecutor);
        this.failedValidationExecutor = Objects.requireNonNull(failedValidationExecutor);
    }

    @Override
    protected InsistVerifiableFactory<X> getFactory(Supplier<String> messageSupplier,
                                                    FailedValidationExecutor<X> failedValidationExecutor) {
        return new InsistVerifiableFactory<>(messageSupplier,
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
        return new InsistCompletableFutureImpl<>(getMessageSupplier(), failedValidationExecutor, wait);
    }

    /**
     * Like calling {@link #withWait(PatientWait)} with a wait object that retries
     * repeatedly up to the set tryingFor maximum with a short delay between
     * attempts. Any throwable thrown during the execution will be ignored. Note that this
     * does NOT interrupt the attempt to get a valid result but checks the timeout between
     * successive attempts to get a true value.
     *
     * @param tryingFor the {@link Duration} object to be used as the maximum
     *                  time to wait.
     *                  May not be null or negative.
     *
     * @return an InsistCompletableFuture instance initialized with the default wait object
     * and the given timeout.
     *
     * @throws IllegalArgumentException if tryingFor is null or negative.
     */
    public InsistFuture<X> within(Duration tryingFor) {
        validate().that(tryingFor).isAtLeast(Duration.ZERO);
        return withWait(DEFAULT_WAIT).within(tryingFor);
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
