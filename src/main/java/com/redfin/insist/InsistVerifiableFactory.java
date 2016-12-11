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
import com.redfin.validity.FailedValidationExecutor;
import com.redfin.validity.Validity;
import com.redfin.validity.VerifiableFactory;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

public final class InsistVerifiableFactory<X extends Throwable> extends VerifiableFactory<X> implements InsistCompletableFuture<X> {

    private static final PatientWait WAIT = PatientWait.builder().build();

    private final BiFunction<String, Throwable, X> throwableFunction;

    InsistVerifiableFactory(String message, BiFunction<String, Throwable, X> throwableFunction, FailedValidationExecutor<X> failedValidationExecutor) {
        super(message, failedValidationExecutor);
        this.throwableFunction = throwableFunction;
    }

    public InsistCompletableFuture<X> withWait(PatientWait wait) {
        Validity.require().that(wait).isNotNull();
        return new InsistFutureImpl<>(throwableFunction, getMessage(), wait);
    }

    @Override
    public InsistFuture<X> within(Duration timeout) {
        Validity.require().that(timeout).isGreaterThanOrEqualTo(Duration.ZERO);
        return withWait(WAIT).within(timeout);
    }

    @Override
    public void that(BooleanSupplier supplier) throws X {
        Validity.require().that(supplier).isNotNull();
        withWait(WAIT).that(supplier);
    }
}
