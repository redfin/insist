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
import org.opentest4j.AssertionFailedError;

final class AssertionFactoryTest {

    @Test
    void testAssertionFactoryCanBeInstantiated() {
        Assertions.assertNotNull(new AssertionFactory("hello"),
                                 "Should be able to instantiate an AssertionFactory with a message prefix");
    }

    @Test
    void testAssertionFactoryCanBeInstantiatedWithNullMessage() {
        Assertions.assertNotNull(new AssertionFactory(null),
                                 "Should be able to instantiate an AssertionFactory with a null message prefix");
    }

    @Test
    void testAssertionFactoryThrowablesAreAssertionFailedErrors() {
        String prefix = "hello";
        AssertionFactory factory = new AssertionFactory(prefix);
        Assertions.assertThrows(AssertionFailedError.class,
                                () -> factory.that(true).isFalse());
    }

    @Test
    void testAssertionFactoryThrowablesContainGivenMessagePrefix() {
        String prefix = "hello";
        AssertionFactory factory = new AssertionFactory(prefix);
        AssertionFailedError error = Assertions.assertThrows(AssertionFailedError.class,
                                                             () -> factory.that(true).isFalse());
        Assertions.assertNotNull(error.getMessage(),
                                 "Errors from an AssertionFactory should not have a null message");
        Assertions.assertTrue(error.getMessage().contains(prefix),
                              "Errors from an AssertionFactory should contain the given message prefix");
    }
}
