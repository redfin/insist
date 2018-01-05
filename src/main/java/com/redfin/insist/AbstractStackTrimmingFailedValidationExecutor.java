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

import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * Base class for {@link FailedValidationExecutor} instances that will be trimming out all except for the first
 * non-Insist, non-Validity libraries line from the stack trace.
 *
 * @param <X> the type of the Throwable it will throw.
 */
public abstract class AbstractStackTrimmingFailedValidationExecutor<X extends Throwable>
           implements FailedValidationExecutor<X> {

    private static final String PACKAGE_NAME = AssertionFailedValidationExecutor.class.getPackage().getName() + ".";
    private static final String VALIDITY_PACKAGE_NAME = Validity.class.getPackage().getName() + ".";

    protected abstract String getDefaultMessage();

    protected abstract <T> X buildThrowable(String expected,
                                            T actual,
                                            String message);

    @Override
    public final <T> void fail(String expected,
                               T actual,
                               Supplier<String> messageSupplier) throws X {
        if (null == expected) {
            throw new NullPointerException(ValidityUtils.nullArgumentMessage("expected"));
        }
        if (null == messageSupplier) {
            throw new NullPointerException(ValidityUtils.nullArgumentMessage("messageSupplier"));
        }
        String message = messageSupplier.get();
        if (null == message) {
            message = getDefaultMessage();
        }
        // Create the throwable
        X throwable = buildThrowable(expected, actual, message);
        if (null == throwable) {
            throw new NullPointerException(ValidityUtils.nullThrowableFromFunction());
        }
        // Find the index of the last stack frame from the insist library
        StackTraceElement[] elements = throwable.getStackTrace();
        StackTraceElement caller = null;
        OptionalInt lastIndex = OptionalInt.empty();
        if (null != elements) {
            for (int i = 0; i < elements.length; i++) {
                StackTraceElement element = elements[i];
                // we want to remove any validity or insist stack frames
                // when locating the caller
                if (null != element.getClassName()
                    && (element.getClassName().startsWith(PACKAGE_NAME)
                        || element.getClassName().startsWith(VALIDITY_PACKAGE_NAME))) {
                    lastIndex = OptionalInt.of(i);
                }
            }
            // Set the caller element, if any found
            if (lastIndex.isPresent()) {
                int index = lastIndex.getAsInt() + 1;
                if (index < elements.length) {
                    caller = elements[index];
                }
            }
        }
        // Set the new stack trace and throw
        StackTraceElement[] newStackTrace;
        newStackTrace = (null == caller) ? new StackTraceElement[]{} : new StackTraceElement[]{caller};
        throwable.setStackTrace(newStackTrace);
        throw throwable;
    }
}
