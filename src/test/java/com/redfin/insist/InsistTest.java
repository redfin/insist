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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DisplayName("The Insist class")
final class InsistTest {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Nested
    @DisplayName("when asserts() is called")
    final class AssertsTests {

        @Test
        @DisplayName("returns a non-null validation factory")
        void testReturnsNonNull() {
            Assertions.assertNotNull(Insist.asserts(),
                                     "Should return a non-null validation factory for asserts()");
        }

        @Test
        @DisplayName("returns the same instance each time")
        void testReturnsSameInstance() {
            Assertions.assertSame(Insist.asserts(),
                                  Insist.asserts(),
                                  "Each call to asserts() should return the same instance.");
        }
    }

    @Nested
    @DisplayName("when assumes() is called")
    final class AssumesTests {

        @Test
        @DisplayName("returns a non-null validation factory")
        void testReturnsNonNull() {
            Assertions.assertNotNull(Insist.assumes(),
                                     "Should return a non-null validation factory for assumes()");
        }

        @Test
        @DisplayName("returns the same instance each time")
        void testReturnsSameInstance() {
            Assertions.assertSame(Insist.assumes(),
                                  Insist.assumes(),
                                  "Each call to assumes() should return the same instance.");
        }
    }

    @Nested
    @DisplayName("is not instantiable and")
    final class NonInstantiableTests {

        @Test
        @DisplayName("is marked as final")
        void testClassIsMarkedAsFinal() {
            Assertions.assertTrue(Modifier.isFinal(Insist.class.getModifiers()),
                                  "A non instantiable class should be marked as final.");
        }

        @Test
        @DisplayName("has only Object as a super class")
        void testClassExtendsObject() {
            Assertions.assertEquals(Object.class,
                                    Insist.class.getSuperclass(),
                                    "A non instantiable class should only extend Object.");
        }

        @Test
        @DisplayName("does not implement any interfaces")
        void testClassDoesNotImplementInterfaces() {
            Assertions.assertEquals(0,
                                    Insist.class.getInterfaces().length,
                                    "A non instantiable class should not implement any interfaces.");
        }

        @Test
        @DisplayName("has only static fields")
        void testClassOnlyHasStaticMembers() {
            List<Field> fields = new ArrayList<>();
            Class<?> clazz = Insist.class;
            while (clazz != Object.class) {
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass();
            }
            Assertions.assertAll("All fields of a non-instantiable class should be static",
                                 fields.stream()
                                       .map(field -> () -> Assertions.assertTrue(Modifier.isStatic(field.getModifiers()),
                                                                                 "field [" + field.getName() + "] should be static")));
        }

        @Test
        @DisplayName("has only static methods")
        void testClassOnlyHasStaticMethods() {
            List<Method> methods = new ArrayList<>();
            Class<?> clazz = Insist.class;
            while (clazz != Object.class) {
                methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
                clazz = clazz.getSuperclass();
            }
            Assertions.assertAll("All methods of a non-instantiable class should be static",
                                 methods.stream()
                                        .map(method -> () -> Assertions.assertTrue(Modifier.isStatic(method.getModifiers()),
                                                                                   "method [" + method.getName() + "] should be static")));
        }

        @Nested
        @DisplayName("has a declared constructor")
        final class ConstructorTests {

            @Test
            @DisplayName("but only a single one")
            void testClassHasOnlyOneConstructor() {
                Assertions.assertEquals(1,
                                        Insist.class.getDeclaredConstructors().length,
                                        "A non instantiable class should only have 1 constructor.");
            }

            @Test
            @DisplayName("that takes in no arguments")
            void testClassHasTheZeroArgumentConstructor() throws NoSuchMethodException {
                Assertions.assertNotNull(Insist.class.getDeclaredConstructor(),
                                         "A non instantiable class should have a declared zero argument constructor.");
            }

            @Test
            @DisplayName("that is marked as private")
            void testClassSingleConstructorIsPrivate() throws NoSuchMethodException {
                Assertions.assertTrue(Modifier.isPrivate(Insist.class.getDeclaredConstructor().getModifiers()),
                                      "A non instantiable class should have it's declared zero argument constructor be private.");
            }

            @Test
            @DisplayName("that throws an error if called via reflection")
            void testClassThrowsAssertionErrorIfConstructorIsCalled() throws NoSuchMethodException {
                Constructor constructor = Insist.class.getDeclaredConstructor();
                Assertions.assertTrue(() -> {
                                          Throwable thrown = null;
                                          try {
                                              constructor.setAccessible(true);
                                              constructor.newInstance();
                                          } catch (Throwable t) {
                                              thrown = t;
                                          }
                                          return null != thrown &&
                                                 thrown instanceof InvocationTargetException &&
                                                 null != thrown.getCause() &&
                                                 thrown.getCause() instanceof AssertionError;
                                      },
                                      "A non instantiable class should throw an AssertionError if the private constructor is called via reflection");
            }
        }
    }
}
