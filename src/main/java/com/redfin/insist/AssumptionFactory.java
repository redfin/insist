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

import com.redfin.validity.FailedValidationExecutor;
import com.redfin.validity.VerifiableFactory;
import org.opentest4j.TestAbortedException;

/**
 * Final subclass of the {@link VerifiableFactory} from the Validity project. This
 * particular factory will throw {@link TestAbortedException}s on validation failure
 * making it useful as the base of an Assumption like library. This would be used
 * to signal that a test state or value has failed to meet some precondition.
 * The verifiable objects created by this factory will use the
 * {@link StackTrimmingFailedValidationExecutor}.
 */
public final class AssumptionFactory extends VerifiableFactory<TestAbortedException> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final FailedValidationExecutor<TestAbortedException> ASSUME_FAILURE = new StackTrimmingFailedValidationExecutor<>(TestAbortedException::new);

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Create a new {@link AssumptionFactory} instance with the given message prefix.
     * A null message results in a factory that will use the default message from
     * the Validity library.
     *
     * @param message the String custom message prefix for the validation on failure.
     *                Null means that the default will be used.
     */
    AssumptionFactory(String message) {
        super(message, ASSUME_FAILURE);
    }
}
