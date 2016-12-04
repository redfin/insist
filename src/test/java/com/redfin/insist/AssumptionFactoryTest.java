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
import org.opentest4j.TestAbortedException;

final class AssumptionFactoryTest {

    @Test
    void testAssumptionFactoryCanBeInstantiated() {
        Assertions.assertNotNull(new AssumptionFactory("hello"),
                                 "Should be able to instantiate an AssertionFactory with a message prefix");
    }

    @Test
    void testAssumptionFactoryCanBeInstantiatedWithNullMessage() {
        Assertions.assertNotNull(new AssumptionFactory(null),
                                 "Should be able to instantiate an AssertionFactory with a null message prefix");
    }

    @Test
    void testAssumptionFactoryThrowablesAreAssertionFailedErrors() {
        String prefix = "hello";
        AssumptionFactory factory = new AssumptionFactory(prefix);
        Assertions.assertThrows(TestAbortedException.class,
                                () -> factory.that(true).isFalse());
    }

    @Test
    void testAssumptionFactoryThrowablesContainGivenMessagePrefix() {
        String prefix = "hello";
        AssumptionFactory factory = new AssumptionFactory(prefix);
        TestAbortedException error = Assertions.assertThrows(TestAbortedException.class,
                                                             () -> factory.that(true).isFalse());
        Assertions.assertNotNull(error.getMessage(),
                                 "Errors from an AssertionFactory should not have a null message");
        Assertions.assertTrue(error.getMessage().contains(prefix),
                              "Errors from an AssertionFactory should contain the given message prefix");
    }
}
