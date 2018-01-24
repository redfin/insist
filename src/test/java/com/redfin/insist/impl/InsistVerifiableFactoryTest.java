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

package com.redfin.insist.impl;

import com.redfin.insist.executor.AssertionFailedValidationExecutor;
import com.redfin.patience.PatientRetry;
import com.redfin.patience.PatientWait;
import com.redfin.validity.FailedValidationExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

@DisplayName("An InsistVerifiableFactory")
final class InsistVerifiableFactoryTest {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test constants, requirements, and helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private InsistVerifiableFactory<AssertionFailedError> getInstance() {
        return getInstance(getMessageSupplier("hello"), getFailedValidationExecutor());
    }

    private InsistVerifiableFactory<AssertionFailedError> getInstance(Supplier<String> messageSupplier,
                                                                      FailedValidationExecutor<AssertionFailedError> failedValidationExecutor) {
        return new InsistVerifiableFactory<>(messageSupplier, failedValidationExecutor);
    }

    private static Supplier<String> getMessageSupplier(String message) {
        return () -> message;
    }

    private static FailedValidationExecutor<AssertionFailedError> getFailedValidationExecutor() {
        return new AssertionFailedValidationExecutor();
    }

    private static final class ValidArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(getMessageSupplier("hello"), getFailedValidationExecutor()),
                             Arguments.of(getMessageSupplier(null), getFailedValidationExecutor()));
        }
    }

    private static final class InvalidArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(null, getFailedValidationExecutor()),
                             Arguments.of(getMessageSupplier("hello"), null));
        }
    }

    private static final class ValidWithinDurations
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(Duration.ZERO),
                             Arguments.of(Duration.ofMillis(100)));
        }
    }

    private static final class InvalidWithinDurations
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of((Duration) null),
                             Arguments.of(Duration.ofMillis(-100)));
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Nested
    @DisplayName("when constructed")
    final class ConstructorTests {

        @ParameterizedTest
        @DisplayName("returns a non-null instance successfully for valid arguments")
        @ArgumentsSource(ValidArgumentsProvider.class)
        void testIsSuccessfulForValidArguments(Supplier<String> messageSupplier,
                                               FailedValidationExecutor<AssertionFailedError> failedValidationExecutor) {
            try {
                Assertions.assertNotNull(getInstance(messageSupplier, failedValidationExecutor),
                                         "Should have returned a non-null instance for valid arguments.");
            } catch (Throwable thrown) {
                Assertions.fail("Should have successfully constructed an instance but caught: " + thrown);
            }
        }

        @ParameterizedTest
        @DisplayName("throws an exception for invalid arguments")
        @ArgumentsSource(InvalidArgumentsProvider.class)
        void testThrowsExceptionForInvalidArguments(Supplier<String> messageSupplier,
                                                    FailedValidationExecutor<AssertionFailedError> failedValidationExecutor) {
            Assertions.assertThrows(NullPointerException.class,
                                    () -> getInstance(messageSupplier, failedValidationExecutor),
                                    "Should have thrown an exception for invalid arguments.");
        }
    }

    @Nested
    @DisplayName("once constructed")
    final class BehaviorTests {

        @Nested
        @DisplayName("when calling getFactory")
        final class GetFactoryTests {

            @ParameterizedTest
            @DisplayName("returns a non-null instance successfully for valid arguments")
            @ArgumentsSource(ValidArgumentsProvider.class)
            void testGetFactoryReturnsNonNullForValidArguments(Supplier<String> messageSupplier,
                                                               FailedValidationExecutor<AssertionFailedError> failedValidationExecutor) {
                try {
                    Assertions.assertNotNull(getInstance().getFactory(messageSupplier, failedValidationExecutor),
                                             "Should have returned a non-null instance for valid arguments.");
                } catch (Throwable thrown) {
                    Assertions.fail("Should have successfully return an instance but caught: " + thrown);
                }
            }

            @ParameterizedTest
            @DisplayName("throws an exception for invalid arguments")
            @ArgumentsSource(InvalidArgumentsProvider.class)
            void testGetFactoryThrowsForInvalidArguments(Supplier<String> messageSupplier,
                                                         FailedValidationExecutor<AssertionFailedError> failedValidationExecutor) {
                Assertions.assertThrows(NullPointerException.class,
                                        () -> getInstance().getFactory(messageSupplier, failedValidationExecutor),
                                        "Should have thrown an exception for invalid arguments.");
            }
        }

        @Nested
        @DisplayName("for wait futures")
        final class WithTests {

            @Test
            @DisplayName("when withWait(PatientWait) is called it returns successfully for non-null")
            void testWithWaitReturnsForNonNull() {
                Assertions.assertNotNull(getInstance().withWait(mock(PatientWait.class)),
                                         "Should return a non-null instance for withWait(PatientWait).");
            }

            @Test
            @DisplayName("when withWait(PatientWait) is called it throws exception for null")
            void testWithWaitThrowsForNull() {
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> getInstance().withWait(null),
                                        "Should throw an exception for withWait(PatientWait) with a null wait.");
            }

            @ParameterizedTest
            @DisplayName("when within(Duration) is called it returns for valid arguments")
            @ArgumentsSource(ValidWithinDurations.class)
            void testWithinReturnsForValidArguments(Duration timeout) {
                Assertions.assertNotNull(getInstance().within(timeout),
                                         "Should return a non-null instance for valid within(Duration) arguments.");
            }

            @ParameterizedTest
            @DisplayName("when within(Duration) is called it throws for invalid arguments")
            @ArgumentsSource(InvalidWithinDurations.class)
            void testWithinThrowsForInvalidArguments(Duration timeout) {
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> getInstance().within(timeout),
                                        "Should throw an exception for invalid within(Duration) arguments.");
            }
        }

        @Nested
        @DisplayName("for retry futures")
        final class RetryTests {

            @Test
            @DisplayName("when withRetry(PatientRetry) is called it returns successfully for non-null")
            void testWithRetryReturnsForNonNull() {
                Assertions.assertNotNull(getInstance().withRetry(mock(PatientRetry.class)),
                                         "Should return a non-null instance for withRetry(PatientRetry).");
            }

            @Test
            @DisplayName("when withRetry(PatientRetry) is called it throws exception for null")
            void testWithRetryThrowsForNull() {
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> getInstance().withRetry(null),
                                        "Should throw an exception for withRetry(PatientRetry) with a null retry.");
            }

            @ParameterizedTest
            @DisplayName("when within(int) is called it returns for valid arguments")
            @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
            void testWithinReturnsForValidArguments(int numRetries) {
                Assertions.assertNotNull(getInstance().within(numRetries),
                                         "Should return a non-null instance for valid within(int) arguments.");
            }

            @ParameterizedTest
            @DisplayName("when within(int) is called it throws for invalid arguments")
            @ValueSource(ints = {-1, Integer.MIN_VALUE})
            void testWithinThrowsForInvalidArguments(int numRetries) {
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> getInstance().within(numRetries),
                                        "Should throw an exception for invalid within(int) arguments.");
            }
        }

        @Nested
        @DisplayName("for thatThrows")
        final class ThatThrowsTests {

            @Test
            void testReturnsExpectedThrowable() {
                RuntimeException exception = new RuntimeException("hello");
                Assertions.assertSame(exception,
                                      getInstance().thatThrows(RuntimeException.class,
                                                               () -> { throw exception; }),
                                      "Expected thatThrows to return the thrown exception.");
            }

            @Test
            void testReturnsExpectedThrowableSubclass() {
                IllegalArgumentException exception = new IllegalArgumentException("hello");
                Assertions.assertSame(exception,
                                      getInstance().thatThrows(RuntimeException.class,
                                                               () -> { throw exception; }),
                                      "Expected thatThrows to return the thrown exception.");
            }

            @Test
            void testThrowsForNoThrownValue() {
                Assertions.assertThrows(AssertionFailedError.class,
                                        () -> getInstance().thatThrows(RuntimeException.class,
                                                                       () -> {}),
                                        "Should throw expected exception for no throwable of expected type.");
            }

            @Test
            void testThrowsForUnexpectedThrowable() {
                Assertions.assertThrows(AssertionFailedError.class,
                                        () -> getInstance().thatThrows(IOException.class,
                                                                       () -> { throw new RuntimeException(); }),
                                        "Should throw expected exception for no throwable of expected type.");
            }
        }
    }
}
