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
import com.redfin.validity.FailedValidationExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

@DisplayName("An InsistCompletableRetryFutureImpl")
final class InsistCompletableRetryFutureImplTest
    extends AbstractFutureTest<InsistCompletableRetryFutureImpl<AssertionFailedError>> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test constants, requirements, and helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    InsistCompletableRetryFutureImpl<AssertionFailedError> getInstance(String message) {
        return getInstance(() -> message,
                           getFailedValidationExecutor(),
                           getRetry(2));
    }

    private InsistCompletableRetryFutureImpl<AssertionFailedError> getInstance(Supplier<String> messageSupplier,
                                                                               FailedValidationExecutor<AssertionFailedError> failedValidationExecutor,
                                                                               PatientRetry retry) {
        return new InsistCompletableRetryFutureImpl<>(messageSupplier, failedValidationExecutor, retry);
    }

    private static Supplier<String> getMessageSupplier(String message) {
        return () -> message;
    }

    private static FailedValidationExecutor<AssertionFailedError> getFailedValidationExecutor() {
        return new AssertionFailedValidationExecutor();
    }

    private static PatientRetry getRetry(int defaultNumberOfRetries) {
        return PatientRetry.builder()
                           .withDefaultNumberOfRetries(defaultNumberOfRetries)
                           .build();
    }

    private static final class ValidArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(getMessageSupplier("hello"), getFailedValidationExecutor(), getRetry(0)),
                             Arguments.of(getMessageSupplier(null), getFailedValidationExecutor(), getRetry(0)));
        }
    }

    private static final class InvalidArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(null, getFailedValidationExecutor(), getRetry(0)),
                             Arguments.of(getMessageSupplier("hello"), null, getRetry(0)),
                             Arguments.of(getMessageSupplier("hello"), getFailedValidationExecutor(), null));
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Nested
    @DisplayName("when constructed")
    final class ConstructorTests {

        @ParameterizedTest
        @DisplayName("should return a non-null instance for valid arguments")
        @ArgumentsSource(ValidArgumentsProvider.class)
        void testThrowsExceptionForValidArguments(Supplier<String> messageSupplier,
                                                  FailedValidationExecutor<AssertionFailedError> failedErrorFailedValidationExecutor,
                                                  PatientRetry retry) {
            try {
                Assertions.assertNotNull(getInstance(messageSupplier, failedErrorFailedValidationExecutor, retry),
                                         "Should have return a non-null instance for valid arguments.");
            } catch (Throwable thrown) {
                Assertions.fail("Should have been able to create an instance successfully but caught: " + thrown);
            }
        }

        @ParameterizedTest
        @DisplayName("should throw an exception for invalid arguments")
        @ArgumentsSource(InvalidArgumentsProvider.class)
        void testThrowsExceptionForInvalidArguments(Supplier<String> messageSupplier,
                                                    FailedValidationExecutor<AssertionFailedError> failedErrorFailedValidationExecutor,
                                                    PatientRetry retry) {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance(messageSupplier, failedErrorFailedValidationExecutor, retry),
                                    "Should throw an exception for invalid arguments.");
        }
    }

    @Nested
    @DisplayName("once created")
    final class BehaviorTests {

        @ParameterizedTest
        @DisplayName("throws an exception for invalid arguments to within(int)")
        @ValueSource(ints = {-1, -100})
        void testThrowsForInvalidArgumentsToWithin(int numRetries) {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance("hello").within(numRetries),
                                    "Should throw an exception for invalid arguments to within(int)");
        }

        @ParameterizedTest
        @DisplayName("can set the number of attempts via within(int)")
        @ValueSource(ints = {0, 1, 10})
        void testWithinSetsMaxAttempts(int numRetries) {
            AtomicInteger counter = new AtomicInteger(0);
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance("hello").within(numRetries).thatEventually(() -> counter.incrementAndGet() == numRetries * 10),
                                    "Should throw an exception for a supplier that never returns true.");
            Assertions.assertEquals(counter.get(),
                                    numRetries + 1,
                                    "Setting the within(int) value should set how many attempts are made.");
        }
    }
}
