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

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * An InsistFuture represents the final type in patient
 * validation for waiting until a true result is returned.
 *
 * @param <X> the type of Throwable thrown if validation never succeeds.
 */
public interface InsistFuture<X extends Throwable> {

    /**
     * Repeatedly call the boolean supplier as defined by the wait or retry
     * implementation of this instance. If the supplier returns true, then exit
     * normally. If true is never received from the supplier within the set time
     * or number of iterations for this instance, then throw a throwable of type X.
     *
     * @param supplier the supplier of boolean values for validation attempts.
     *                 May not be null.
     *
     * @throws X                        if the supplier never supplies a true
     *                                  value within the given timeout period.
     * @throws IllegalArgumentException if supplier is null.
     */
    void thatEventually(BooleanSupplier supplier) throws X;

    /**
     * Repeatedly call the Optional supplier as defined by the wait or retry
     * implementation of this instance. If the supplier returns a non-empty
     * Optional, then exit normally. If a non-empty Optional is never received
     * from the supplier within the set time or number of iterations for this
     * instance, then throw a throwable of type X.
     *
     * @param supplier the supplier of Optional values for validation attempts.
     *                 May not be null.
     *
     * @throws X                        if the supplier never supplies a non-empty
     *                                  Optional value within the given timeout
     *                                  period.
     * @throws IllegalArgumentException if supplier is null.
     */
    void thatEventuallyIsPresent(Supplier<Optional<?>> supplier) throws X;

    /**
     * Repeatedly call the supplier as defined by the wait or retry implementation
     * of this instance. If the supplier returns a non-null value, then exit
     * normally. If a non-null object is never received from the supplier within
     * the set time or number of iterations for this instance, then throw a
     * throwable of type X.
     *
     * @param supplier the supplier of object values for validation attempts.
     *                 May not be null.
     *
     * @throws X                        if the supplier never supplies a non-null
     *                                  value within the given timeout period.
     * @throws IllegalArgumentException if supplier is null.
     */
    void thatEventuallyIsNotNull(Supplier<?> supplier) throws X;

    /**
     * Repeatedly call the executable as defined by the wait or retry
     * implementation of this instance. If the executable throws a throwable of type T,
     * then exit normally. If a different type of throwable, or no throwable at all is thrown,
     * within the given timeout or number of retries then throw a throwable of type X.
     *
     * @param expectedThrowableClass the class of throwable that is expected.
     *                               May not be null.
     * @param executable             the executable to be repeatedly called.
     *                               May not be null.
     * @param <T>                    the type of expectedThrowableClass.
     *
     * @return the throwable that was expected.
     *
     * @throws X                        the type to be thrown if expectedThrowableClass is never encountered.
     * @throws IllegalArgumentException if expectedThrowableClass or executable are null.
     */
    <T extends Throwable> T thatEventuallyThrows(Class<T> expectedThrowableClass,
                                                 InsistExecutable<T> executable) throws X;
}
