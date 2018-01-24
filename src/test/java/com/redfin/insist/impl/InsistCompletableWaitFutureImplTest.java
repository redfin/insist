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
import com.redfin.patience.PatientWait;
import com.redfin.validity.FailedValidationExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import java.util.stream.Stream;

@DisplayName("An InsistCompletableWaitFutureImpl")
final class InsistCompletableWaitFutureImplTest
    extends AbstractFutureTest<InsistCompletableWaitFutureImpl<AssertionFailedError>> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test constants, requirements, and helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    InsistCompletableWaitFutureImpl<AssertionFailedError> getInstance(String message) {
        return getInstance(() -> message,
                           getFailedValidationExecutor(),
                           PatientWait.builder()
                                      .withDefaultTimeout(Duration.ofMillis(100))
                                      .build());
    }

    private static Supplier<String> getMessageSupplier(String message) {
        return () -> message;
    }

    private static FailedValidationExecutor<AssertionFailedError> getFailedValidationExecutor() {
        return new AssertionFailedValidationExecutor();
    }

    private static PatientWait getWait(Duration defaultTimeout) {
        return PatientWait.builder()
                          .withDefaultTimeout(defaultTimeout)
                          .build();
    }

    private InsistCompletableWaitFutureImpl<AssertionFailedError> getInstance(Supplier<String> messageSupplier,
                                                                              FailedValidationExecutor<AssertionFailedError> failedValidationExecutor,
                                                                              PatientWait wait) {
        return new InsistCompletableWaitFutureImpl<>(messageSupplier, failedValidationExecutor, wait);
    }

    private static final class ValidArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(getMessageSupplier("hello"), getFailedValidationExecutor(), getWait(Duration.ZERO)),
                             Arguments.of(getMessageSupplier(null), getFailedValidationExecutor(), getWait(Duration.ZERO)));
        }
    }

    private static final class InvalidArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(null, getFailedValidationExecutor(), getWait(Duration.ZERO)),
                             Arguments.of(getMessageSupplier("hello"), null, getWait(Duration.ZERO)),
                             Arguments.of(getMessageSupplier("hello"), getFailedValidationExecutor(), null));
        }
    }

    private static final class DurationConverter
                    implements ArgumentConverter {

        @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            return Duration.ofMillis((Integer) source);
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
                                                  PatientWait wait) {
            try {
                Assertions.assertNotNull(getInstance(messageSupplier, failedErrorFailedValidationExecutor, wait),
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
                                                    PatientWait wait) {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance(messageSupplier, failedErrorFailedValidationExecutor, wait),
                                    "Should throw an exception for invalid arguments.");
        }
    }

    @Nested
    @DisplayName("once created")
    final class BehaviorTests {

        @ParameterizedTest
        @DisplayName("throws an exception for invalid arguments to within(Duration)")
        @ValueSource(ints = {-1, -100})
        void testThrowsForInvalidArgumentsToWithin(@ConvertWith(DurationConverter.class) Duration timeout) {
            Assertions.assertThrows(IllegalArgumentException.class,
                                    () -> getInstance("hello").within(timeout),
                                    "Should throw an exception for invalid arguments to within(int)");
        }

        @ParameterizedTest
        @DisplayName("can set the number of attempts via within(Duration)")
        @ValueSource(ints = {0, 1, 10})
        void testWithinSetsMaxAttempts(@ConvertWith(DurationConverter.class) Duration timeout) {
            Instant start = Instant.now();
            Assertions.assertThrows(AssertionFailedError.class,
                                    () -> getInstance("hello").within(timeout).thatEventually(() -> false),
                                    "Should throw an exception for a supplier that never returns true.");
            Instant end = Instant.now();
            Duration time = Duration.between(start, end);
            Assertions.assertTrue(timeout.minus(time).isZero() || timeout.minus(time).isNegative(),
                                  "Setting the within(Duration) value have made the time it took to execute be equal to or greater than the timeout.");
        }
    }
}
