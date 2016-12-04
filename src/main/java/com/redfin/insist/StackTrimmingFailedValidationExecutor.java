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
import com.redfin.validity.Validity;
import com.redfin.validity.ValidityUtils;

import java.util.function.Function;

/**
 * todo
 */
public final class StackTrimmingFailedValidationExecutor<X extends Throwable> implements FailedValidationExecutor<X> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private static final String DEFAULT_MESSAGE = "Insistence failure";
    private static final String MESSAGE_FORMAT = "%s\n    expected : %s\n     subject : <%s>";
    private static final String PACKAGE_NAME = StackTrimmingFailedValidationExecutor.class.getPackage().getName() + ".";

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Members
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private final Function<String, X> throwableFunction;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * todo
     *
     * @param throwableFunction
     *
     * @throws IllegalArgumentException if throwableFunction is null.
     */
    public StackTrimmingFailedValidationExecutor(Function<String, X> throwableFunction) {
        this.throwableFunction = Validity.require().that(throwableFunction).isNotNull();
    }

    @Override
    public <T> void fail(String expected, T subject, String message) throws X {
        if (null == expected) {
            throw new NullPointerException(ValidityUtils.nullArgumentMessage("expected"));
        }
        String subjectDescription = ValidityUtils.describe(subject);
        if (null == message) {
            message = DEFAULT_MESSAGE;
        }
        // Create the throwable
        X throwable = throwableFunction.apply(String.format(MESSAGE_FORMAT, message, expected, subjectDescription));
        if (null == throwable) {
            throw new NullPointerException(ValidityUtils.nullThrowableFromFunction());
        }
        // Trim the stack trace and throw
        StackTraceElement[] elements = throwable.getStackTrace();
        StackTraceElement caller = null;
        if (null != elements) {
            for (StackTraceElement element : elements) {
                if (!element.getClassName()
                            .startsWith(PACKAGE_NAME)) {
                    caller = element;
                    break;
                }
            }
        }
        StackTraceElement[] newStackTrace;
        newStackTrace = (null == caller) ? new StackTraceElement[0] : new StackTraceElement[]{caller};
        throwable.setStackTrace(newStackTrace);
        throw throwable;
    }
}
