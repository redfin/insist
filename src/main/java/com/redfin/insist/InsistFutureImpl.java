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
import com.redfin.validity.Validity;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

final class InsistFutureImpl<X extends Throwable> implements InsistCompletableFuture<X> {

    private static final String CUSTOM_FORMAT = "%s : Timeout reached after %d unsuccessful attempt(s)";
    private static final String DEFAULT_FORMAT = "Timeout reached after %d unsuccessful attempt(s)";

    private final BiFunction<String, Throwable, X> throwableFunction;
    private final String message;
    private final PatientWait wait;

    private Duration timeout;

    InsistFutureImpl(BiFunction<String, Throwable, X> throwableFunction, String message, PatientWait wait) {
        this.throwableFunction = Validity.require().that(throwableFunction).isNotNull();
        this.message = message;
        this.wait = Validity.require().that(wait).isNotNull();
        this.timeout = wait.getDefaultTimeout();
    }

    @Override
    public InsistFuture<X> within(Duration timeout) {
        this.timeout = Validity.require().that(timeout).isGreaterThanOrEqualTo(Duration.ZERO);
        return this;
    }

    @Override
    public void that(BooleanSupplier supplier) throws X {
        Validity.require().that(supplier).isNotNull();
        try {
            // The default filter considers false an invalid result so
            // this will always either find true or throw a timeout exception
            wait.from(supplier::getAsBoolean)
                .get(timeout);
        } catch (PatientTimeoutException exception) {
            // Failure, throw requested throwable type
            throw throwableFunction.apply(fail(message, exception.getNumberAttempts()), exception);
        }
    }

    private static String fail(String message, int numAttempts) {
        if (null == message) {
            return String.format(DEFAULT_FORMAT, numAttempts);
        } else {
            return String.format(CUSTOM_FORMAT, message, numAttempts);
        }
    }
}
