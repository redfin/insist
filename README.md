[![Build Status](https://travis-ci.org/redfin/insist.svg?branch=master)](https://travis-ci.org/redfin/insist)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Insist

## What is Insist?

Insist is an assertion library.
It is an extension of the validation library [Validity](https://github.com/redfin/validity).
Validity offers fluent, strongly-typed validation of values for Java.
It also uses the [Patience](https://github.com/redfin/patience) library for testing conditions that eventually become true.
Insist customizes the failure modes of Validity to more thoroughly remove the failed stack trace.
It also will throw Throwables from the [opentest4j](https://github.com/ota4j-team/opentest4j) project for consistency with other assertion frameworks.

## Installation

To install, you can simply include the dependency from Maven Central:

```xml
<dependency>
    <groupId>com.redfin</groupId>
    <artifactId>insist</artifactId>
    <version>4.3.0</version>
    <scope>test</scope>
</dependency>
```

For best effect, you should statically import the static `Insist` methods.
```java
import static com.redfin.insist.Insist.*;
```

## Assertion vs. Assumption

There are three entry methods when using `Insist`.
They are `assumes()`, `asserts()`, and `expects()` in the `Insist` static class.
The difference is in what will be thrown upon validation failure.

## Stack trace example

`Insist` is designed to be used during test methods directly.
In those cases a too deep inheritance or call stack hierarchy makes stack traces hard to read.
Regular assertion libraries leave the stack trace intact which is full of implementation details of the test runner itself when what failed was an expected condition.
Validity was designed to give better standard comparison messages for failed state checks and by removing the extraneous stack trace elements it leads to cleaner, less noisy errors.
This reduces the "cognitive overhead" when investigating test failures.
Note that due to the removal of stack traces, `Insist` should **only** be used during an actual test method, `Validity` should be used directly during production code or test frameworks to preserve the stack trace behavior needed for more complex debugging.

```java
@Test
public void testAsserts() {
    assertion().that("hello").startsWith("w");
}
```
Results in the following error (that's the full stack trace):
```
java.lang.AssertionError: Subject failed validation
    expected : t -> t.startsWith("w")
     subject : <hello>

	at com.redfin.example.FooTest.testAsserts(FooTest.java:41)
```

## Custom message example

For either assumptions or assertions you can give a custom message prefix, if desired.
The stack trace behavior and the portion of the message that shows the expected and actual values will still be the same, but the user can give a custom message prefix that is put in the first line of the thrown Throwable on failure.
With the following code:

```java
@Test
public void testAsserts() {
    withMessage("hello, world").assertion().that("hello").startsWith("w");
}
```

You would get an error like:

```java
java.lang.AssertionFailedError: Hello, world
    expected : t -> t.startsWith("w")
     subject : <hello>

	at com.redfin.example.FooTest.testAsserts(FooTest.java:41)
```

## Waiting for results example

Insist also has a dependency on the [Patience](https://github.com/redfin/patience) library which allows you to wait for
a valid result to be true. If no valid result is found within a given timeout then
an error or exception will be thrown the same as above.

```java
@Test
public void testEventualAssertsWithWait() {
    withMessage("The value never became true").asserts()
                                              .within(Duration.ofMinutes(1))
                                              .thatEventually(() -> someBooleanSupplier.get());
}

@Test
public void testEventualAssertsWithRetry() {
    withMessage("The value never became true").asserts()
                                              .within(2)
                                              .thatEventually(() -> someBooleanSupplier.get());
}
```

in the above wait example, the method `someBooleanSupplier.get()` will be called every 500 milliseconds (the default)
until either a `true` value is received or until the Duration of 1 minute is reached. If the timeout
is reached than an assertion failure is thrown.
Note that it might stop before 1 minute has been reached if it had a failed attempt and waiting for 500 milliseconds
would put it after the requested timeout as per the Patience library.
Also note that it does not interrupt the retrieval of a boolean from the `thatEventually(BooleanSupplier)` so
if the code you are checking hangs it will hang as well or could take longer than the given duration and be successful.
In the retry version, it will try up to the number of retries plus the initial attempt. So in the above retry if the
boolean supplier always returns false, it will execute the supplier 3 times before throwing an assertion error.
