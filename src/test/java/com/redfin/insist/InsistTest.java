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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

final class InsistTest {

    @Test
    void testNonInstantiableClassHasOnlyOneConstructor() {
        Assertions.assertEquals(1,
                                Insist.class.getDeclaredConstructors().length,
                                "Insist should only have 1 constructor.");
    }

    @Test
    void testNonInstantiableClassHasOnlySingleArgumentConstructor() throws NoSuchMethodException {
        Assertions.assertNotNull(Insist.class.getDeclaredConstructor(),
                                 "Insist should only have the no argument constructor.");
    }

    @Test
    void testNonInstantiableClassSingleConstructorIsPrivate() throws NoSuchMethodException {
        Assertions.assertTrue(Modifier.isPrivate(Insist.class.getDeclaredConstructor().getModifiers()),
                              "Insist should only have a private constructor.");
    }

    @Test
    void testNonInstantiableClassThrowsErrorIfConstructorIsCalled() {
        AssertionError error = Assertions.assertThrows(AssertionError.class,
                                                       () -> {
                                                           try {
                                                               Constructor<Insist> constructor = Insist.class.getDeclaredConstructor();
                                                               constructor.setAccessible(true);
                                                               constructor.newInstance();
                                                           } catch (Throwable thrown) {
                                                               // A constructor error is wrapped, unwrap it
                                                               if (thrown instanceof InvocationTargetException) {
                                                                   throw thrown.getCause();
                                                               }
                                                               throw thrown;
                                                           }
                                                       });
        Assertions.assertEquals("Cannot instantiate the static class Insist",
                                error.getMessage(),
                                "Insist should throw the expected error if the construct is called");
    }
}
