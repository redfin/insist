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

/**
 * An InsistCompletableWaitFuture represents the final mid point in patient
 * validation for waiting until a true result is returned. This type will
 * attempt up to a maximum number of retries.
 *
 * @param <X> the type of Throwable thrown if validation never succeeds.
 */
public interface InsistCompletableRetryFuture<X extends Throwable>
         extends InsistFuture<X> {

    /**
     * @param numRetries the maximum number of times to retry upon unsuccessful validation.
     *                   A value of zero means to try only once.
     *                   May not be negative.
     *
     * @return an {@link InsistFuture} with the given timeout characteristics.
     *
     * @throws IllegalArgumentException if numRetries is negative.
     */
    InsistFuture<X> within(int numRetries);
}
