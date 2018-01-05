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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class AbstractStackTrimmingFailedValidationExecutorTest {

    private static class MockImplementation
                 extends AbstractStackTrimmingFailedValidationExecutor<IllegalArgumentException> {

        @Override
        protected String getDefaultMessage() {
            return "hello";
        }

        @Override
        protected <T> IllegalArgumentException buildThrowable(String expected,
                                                              T actual,
                                                              String message) {
            return null;
        }
    }

    @Test
    void testFailThrowsForNullThrowableReturnedbySubclass() {
        Assertions.assertThrows(NullPointerException.class,
                                () -> new MockImplementation().fail("t -> t.equals(\"hello\")", "world", () -> "default message"),
                                "AbstractStackTrimmingFailedValidationExecutor should throw an exception if buildThrowable returns a null throwable.");
    }
}
