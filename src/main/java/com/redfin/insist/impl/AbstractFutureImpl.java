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

import com.redfin.insist.InsistExecutable;
import com.redfin.insist.InsistFuture;
import com.redfin.patience.PatientExecutable;

import java.util.Optional;
import java.util.function.Supplier;

import static com.redfin.validity.Validity.validate;

/**
 * A helper base class for implementations of the {@link InsistFuture} interface.
 *
 * @param <X> the type of Throwable to be thrown if validation fails.
 */
abstract class AbstractFutureImpl<X extends Throwable>
    implements InsistFuture<X> {

    /**
     * This creates a {@link PatientExecutable} that returns the thrown Throwable of
     * type {@code T} if the given executable throws it or null otherwise.
     *
     * @param expectedThrowableClass the type of throwable expected.
     *                               May not be null.
     * @param executable             the executable to run.
     *                               May not be null.
     * @param <T>                    the type of the expected throwable.
     *
     * @return an executable that makes sure that the given executable throws the
     * given throwable class.
     *
     * @throws IllegalArgumentException if either argument is null.
     */
    @SuppressWarnings("unchecked")
    final <T extends Throwable> PatientExecutable<T> getEventuallyThrowsExecutable(Class<T> expectedThrowableClass,
                                                                                   InsistExecutable<?> executable) {
        validate().that(expectedThrowableClass).isNotNull();
        validate().that(executable).isNotNull();
        return () -> {
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
        };
    }

    @Override
    public void thatEventuallyIsPresent(Supplier<Optional<?>> supplier) throws X {
        validate().that(supplier).isNotNull();
        this.thatEventually(() ->
                Optional.of(supplier)
                        .flatMap(Supplier::get)
                        .isPresent()
        );
    }

    @Override
    public void thatEventuallyIsNotNull(Supplier<?> supplier) throws X {
        validate().that(supplier).isNotNull();
        this.thatEventually(() -> supplier.get() != null);
    }
}
