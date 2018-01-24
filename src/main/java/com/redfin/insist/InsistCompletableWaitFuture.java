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

import java.time.Duration;

/**
 * An InsistCompletableWaitFuture represents the final mid point in patient
 * validation for waiting until a true result is returned. This type will
 * keep trying up to some timeout.
 *
 * @param <X> the type of Throwable thrown if validation never succeeds.
 */
public interface InsistCompletableWaitFuture<X extends Throwable>
         extends InsistFuture<X> {

    /**
     * @param timeout the maximum {@link Duration} to wait for successful validation.
     *
     * @return an {@link InsistFuture} with the given timeout characteristics.
     *
     * @throws IllegalArgumentException if timeout is null or negative.
     */
    InsistFuture<X> within(Duration timeout);
}
