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

package com.redfin.insist.executor;

import com.redfin.validity.ValidityUtils;
import org.opentest4j.TestAbortedException;

/**
 * Concrete subclass of the {@link AbstractStackTrimmingFailedValidationExecutor} that
 * throws {@link TestAbortedException} exceptions upon failure.
 */
public final class AbortedFailedValidationExecutor
           extends AbstractStackTrimmingFailedValidationExecutor<TestAbortedException> {

    @Override
    protected String getDefaultMessage() {
        return "Test aborted";
    }

    @Override
    protected <T> TestAbortedException buildThrowable(String expected,
                                                      T actual,
                                                      String message) {
        if (null == expected) {
            throw new NullPointerException(ValidityUtils.nullArgumentMessage("expected"));
        }
        if (null == message) {
            throw new NullPointerException(ValidityUtils.nullArgumentMessage("message"));
        }
        return new TestAbortedException(String.format("%s\n  expected : %s\n    actual : <%s>",
                                                      message,
                                                      expected,
                                                      ValidityUtils.describe(actual)));
    }
}
