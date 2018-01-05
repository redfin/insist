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

import com.redfin.patience.PatientWait;
import com.redfin.validity.FailedValidationExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;
import java.util.function.Supplier;

final class InsistVerifiableFactoryTest {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private InsistVerifiableFactory<AssertionFailedError> getInstance(Supplier<String> messageSupplier) {
        return getInstance(messageSupplier, new AssertionFailedValidationExecutor());
    }

    private InsistVerifiableFactory<AssertionFailedError> getInstance(Supplier<String> messageSupplier,
                                                                      FailedValidationExecutor<AssertionFailedError> failedValidationExecutor) {
        return new InsistVerifiableFactory<>(messageSupplier,
                                             failedValidationExecutor);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Test cases
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    void testCanInstantiateInsistVerifiableFactory() {
        Assertions.assertNotNull(getInstance(() -> "message"),
                                 "Should be able to instantiate an InsistVerifiableFactory.");
    }

    @Test
    void testCanInstantiateWithNullMessage() {
        Assertions.assertNotNull(getInstance(() -> null),
                                 "Should be able to instantiate an InsistVerifiableFactory with a null message.");
    }

    @Test
    void testConstructorThrowsExceptionForNullFailedValidationExecutor() {
        Assertions.assertThrows(NullPointerException.class,
                                () -> new InsistVerifiableFactory<>(() -> "hello",
                                                                    null),
                                "InsistVerifiableFactory constructor should throw exception for null failed validation executor.");
    }

    @Test
    void testGetFactoryReturnsANonNullInstance() {
        Assertions.assertNotNull(getInstance(() -> "message").getFactory(() -> "newMessage",
                                                                   new AssertionFailedValidationExecutor()),
                                 "InsistVerifiableFactory getFactory should return a non-null instance.");
    }

    @Test
    void testGetFactoryThrowsExceptionForNullFailedValidationExecutor() {
        Assertions.assertThrows(NullPointerException.class,
                                () -> getInstance(() -> "message").getFactory(() -> "newMessage", null),
                                "InsistVerifiableFactory getFactory should throw exception for null failed validation executor.");
    }

    @Test
    void testWithWaitReturnsANonNullInstance() {
        Assertions.assertNotNull(getInstance(() -> "message").withWait(PatientWait.builder().build()),
                                 "InsistVerifiableFactory withWait should return a non-null instance.");
    }

    @Test
    void testWithWaitThrowsExceptionForNullWait() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInstance(() -> "message").withWait(null),
                                "InsistVerifiableFactory withWait should throw exception for null wait.");
    }

    @Test
    void testWithinReturnsANonNullInstance() {
        Assertions.assertNotNull(getInstance(() -> "message").within(Duration.ofMillis(100)),
                                 "InsistVerifiableFactory within should return a non-null instance.");
    }

    @Test
    void testWithinThrowsExceptionForNullTryingFor() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInstance(() -> "message").within(null),
                                "InsistVerifiableFactory within should throw exception for null tryingFor.");
    }

    @Test
    void testWithinThrowsExceptionForNegativeTryingFor() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInstance(() -> "message").within(Duration.ofMillis(100).negated()),
                                "InsistVerifiableFactory within should throw exception for negative tryingFor.");
    }

    @Test
    void testThatThrowsExceptionForNullExpectedClass() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInstance(() -> "message").thatThrows(null, () -> {}),
                                "InsistVerifiableFactory thatThrows should throw exception for null expected class.");
    }

    @Test
    void testThatThrowsThrowsExceptionForNullExecutable() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInstance(() -> "message").thatThrows(IllegalArgumentException.class, null),
                                "InsistVerifiableFactory thatThrows should throw exception for null executable.");
    }

    @Test
    void testThatThrowsThrowsExceptionForExecutableThatDoesNotThrowExpected() {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> getInstance(() -> "message").thatThrows(null, () -> {}),
                                "InsistVerifiableFactory thatThrows should throw exception for null expected class.");

    }

    @Test
    void testThatThrowsReturnsExpectedThrowableIfThrown() {
        String message = "whoops";
        Assertions.assertEquals(message,
                                getInstance(() -> "message").thatThrows(IllegalArgumentException.class,
                                                                  () -> { throw new IllegalArgumentException("whoops"); })
                                                      .getMessage(),
                                "InsistFuture thatEventuallyThrows should return the thrown expected exception.");
    }
}
