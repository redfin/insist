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

import com.redfin.validity.AbstractVerifiableFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

final class InsistTest {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void testInsistAssumeReturnsNonNullFactory() {
        Assertions.assertNotNull(Insist.assume(),
                                 "Insist.assume() should return a non-null factory.");
    }

    @Test
    void testInsistAssumeReturnsNullMessageFactory() throws Exception {
        AbstractVerifiableFactory<?, ?> factory = Insist.assume();
        Field field = factory.getClass().getSuperclass().getDeclaredField("message");
        field.setAccessible(true);
        Assertions.assertNull(field.get(factory),
                              "Insist.assume() should return a factory with a null message.");
    }

    @Test
    void testInsistAssertsReturnsNonNullFactory() {
        Assertions.assertNotNull(Insist.asserts(),
                                 "Insist.asserts() should return a non-null factory.");
    }

    @Test
    void testInsistAssertsReturnsNullMessageFactory() throws Exception {
        AbstractVerifiableFactory<?, ?> factory = Insist.asserts();
        Field field = factory.getClass().getSuperclass().getDeclaredField("message");
        field.setAccessible(true);
        Assertions.assertNull(field.get(factory),
                              "Insist.asserts() should return a factory with a null message.");
    }

    @Test
    void testInsistIsMarkedAsFinal() {
        Assertions.assertTrue(Modifier.isFinal(Insist.class.getModifiers()),
                              "The Insist class should be marked as final");
    }

    @Test
    void testInsistHasOnlyOneConstructor() {
        Assertions.assertTrue(Insist.class.getDeclaredConstructors().length == 1,
                              "The Insist class should only have 1 constructor");
    }

    @Test
    void testInsistHasTheZeroArgumentConstructor() throws NoSuchMethodException {
        Assertions.assertTrue(null != Insist.class.getDeclaredConstructor(),
                              "The Insist class should have a zero argument constructor");
    }

    @Test
    void testInsistSingleConstructorIsPrivate() throws NoSuchMethodException {
        Assertions.assertTrue(Modifier.isPrivate(Insist.class.getDeclaredConstructor().getModifiers()),
                              "The Insist class should have a private zero argument constructor");
    }

    @Test
    void testInsistThrowsAssertionErrorIfConstructorIsCalled() throws NoSuchMethodException {
        Predicate<Constructor<?>> predicate = constructor -> {
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
        };
        Assertions.assertTrue(predicate.test(Insist.class.getDeclaredConstructor()),
                              "The Insist class should throw an AssertionError if the private constructor is called via reflection");
    }

    @Test
    void testClassOnlyHasStaticMembers() {
        List<Field> fields = new ArrayList<>();
        Class<?> clazz = Insist.class;
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(Insist.class.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        Assertions.assertAll("All fields of a non-instantiable class should be static",
                             fields.stream()
                                   .map(field -> () -> Assertions.assertTrue(Modifier.isStatic(field.getModifiers()),
                                                                             String.format("field [%s] should be static", field.getName()))));
    }

    @Test
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
                                                                               String.format("method [%s] should be static", method.getName()))));
    }

    @Test
    void testAssertFailureThrowsAssertFailure() {
        Assertions.assertThrows(AssertionFailedError.class,
                                () -> Insist.asserts().that(true).isFalse(),
                                "An assert failure should throw a TestSkippedException.");
    }

    @Test
    void testAssumeFailureThrowsSkipException() {
        Assertions.assertThrows(TestAbortedException.class,
                                () -> Insist.assume().that(true).isFalse(),
                                "An assume failure should throw a TestAbortedException.");
    }
}
