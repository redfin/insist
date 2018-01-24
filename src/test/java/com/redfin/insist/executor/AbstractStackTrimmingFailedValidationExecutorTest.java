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

import com.redfin.fuzzy.Any;
import com.redfin.validity.ValidityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

abstract class AbstractStackTrimmingFailedValidationExecutorTest<X extends Throwable,
                                                                 T extends AbstractStackTrimmingFailedValidationExecutor<X>> {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test constants, requirements, and helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * @return an instance of the class under test.
     */
    abstract T getInstance();

    /**
     * @return the Class object of the class under test.
     */
    abstract Class<T> getInstanceClass();

    /**
     * @return the expected throwable type from the fail method.
     */
    abstract Class<X> getExpectedThrowableClass();

    private static Supplier<String> createSupplier(String message) {
        return () -> message;
    }

    private static String generateRandomNonEmptyString() {
        // For deterministic results, add a seed to this random
        Random random = new Random();
        return Any.string().nonEmpty().generateAnyOnce(random);
    }

    private static final class ValidFailArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), createSupplier(generateRandomNonEmptyString())),
                             // A message supplier that returns null is valid
                             Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), createSupplier(null)),
                             // An empty string for any value is valid
                             Arguments.of("", generateRandomNonEmptyString(), createSupplier(generateRandomNonEmptyString())),
                             Arguments.of(generateRandomNonEmptyString(), "", createSupplier(generateRandomNonEmptyString())),
                             Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), createSupplier("")));
        }
    }

    private static final class InvalidFailArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            // The only invalid arguments to fail are a null expected or a null message supplier
            return Stream.of(Arguments.of(null, generateRandomNonEmptyString(), createSupplier(generateRandomNonEmptyString())),
                             Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), null));
        }
    }

    private static final class ValidBuildThrowableArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            // Actual may be null but the other arguments may not be
            return Stream.of(Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), generateRandomNonEmptyString()),
                             Arguments.of("", generateRandomNonEmptyString(), generateRandomNonEmptyString()),
                             Arguments.of(generateRandomNonEmptyString(), "", generateRandomNonEmptyString()),
                             Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), ""),
                             Arguments.of(generateRandomNonEmptyString(), null, generateRandomNonEmptyString()));
        }
    }

    private static final class InvalidBuildThrowableArgumentsProvider
                    implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            // Invalid arguments are either a null expected or a null message
            return Stream.of(Arguments.of(null, generateRandomNonEmptyString(), generateRandomNonEmptyString()),
                             Arguments.of(generateRandomNonEmptyString(), generateRandomNonEmptyString(), null));
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    @DisplayName("can be instantiated")
    void testCanInstantiate() {
        try {
            Assertions.assertNotNull(getInstance(),
                                     "Should be able to instantiate the class under test.");
        } catch (Throwable thrown) {
            Assertions.fail("Should be able to instantiate but caught throwable: " + thrown);
        }
    }

    @Nested
    @DisplayName("returns a default message")
    final class DefaultMessageTests {

        @Test
        @DisplayName("that isn't null")
        void testReturnsNonNull() {
            Assertions.assertNotNull(getInstance().getDefaultMessage(),
                                     "Should return a non-null default message.");
        }

        @Test
        @DisplayName("that isn't empty")
        void returnsNonEmpty() {
            String message = getInstance().getDefaultMessage();
            Assumptions.assumeTrue(null != message,
                                   "The default message should not be null.");
            Assertions.assertFalse(message.isEmpty(),
                                   "The default message should not be empty.");
        }
    }

    @Nested
    @DisplayName("builds a throwable")
    final class BuildThrowableTests {

        @ParameterizedTest
        @DisplayName("that isn't null with valid arguments")
        @ArgumentsSource(ValidBuildThrowableArgumentsProvider.class)
        void testReturnsNonNullThrowable(String expected,
                                         String actual,
                                         String message) {
            Assertions.assertNotNull(getInstance().buildThrowable(expected, actual, message),
                                     "Should return a non-null throwable.");
        }

        @ParameterizedTest
        @DisplayName("but throws a NullPointerException for invalid arguments.")
        @ArgumentsSource(InvalidBuildThrowableArgumentsProvider.class)
        void testThrowsForInvalidArguments(String expected,
                                           String actual,
                                           String message) {
            Assertions.assertThrows(NullPointerException.class,
                                    () -> getInstance().buildThrowable(expected, actual, message),
                                    "Should throw an exception for an invalid argument.");
        }
    }

    @Nested
    @DisplayName("when fail is called")
    final class FailTests {

        @ParameterizedTest
        @DisplayName("with valid arguments it throws the expected throwable type")
        @ArgumentsSource(ValidFailArgumentsProvider.class)
        void testThrowsExpectedWithValidArguments(String expected,
                                                  String actual,
                                                  Supplier<String> messageSupplier) {
            Assertions.assertThrows(getExpectedThrowableClass(),
                                    () -> getInstance().fail(expected, actual, messageSupplier),
                                    "Should throw an instance of the expected throwable type for valid arguments to fail.");
        }

        @ParameterizedTest
        @DisplayName("with invalid arguments it throws a NullPointerException")
        @ArgumentsSource(InvalidFailArgumentsProvider.class)
        void testThrowsExceptionForInvalidArguments(String expected,
                                                    String actual,
                                                    Supplier<String> messageSupplier) {
            Assertions.assertThrows(NullPointerException.class,
                                    () -> getInstance().fail(expected, actual, messageSupplier),
                                    "Should throw a NullPointerException for invalid arguments to fail.");
        }

        @Test
        @DisplayName("throws NullPointerException if the build throwable method returns null")
        void testThrowsExceptionForNullBuildThrowable() {
            T instance = spy(getInstanceClass());
            when(instance.buildThrowable("foo", "bar", "message")).thenReturn(null);
            Assertions.assertEquals(ValidityUtils.nullThrowableFromFunction(),
                                    Assertions.assertThrows(NullPointerException.class,
                                                            () -> instance.fail("foo", "bar", () -> "message"),
                                                            "A null throwable from buildThrowable should throw a NullPointerException.")
                                              .getMessage(),
                                    "A null throwable from buildThrowable should throw a NullPointerException with the expected message.");
        }
    }
}
