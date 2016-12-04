[![Build Status](https://travis-ci.org/redfin/insist.svg?branch=master)](https://travis-ci.org/redfin/insist)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# Insist

## What is Insist?

Insist is an extension of the validation library [Validity](https://github.com/redfin/validity).
Validity offers fluent, strongly-typed validation of values for Java.
Insist customizes the failure modes of Validity to more thoroughly remove the failed stack trace.
It also will throw Throwables from the [opentest4j](https://github.com/ota4j-team/opentest4j) project for consistency with other assertion frameworks.

## Installation

To install, you can simply include the dependency from Maven Central:

```xml
<dependency>
    <groupId>com.redfin</groupId>
    <artifactId>insist</artifactId>
    <version>0.1.0-beta</version>
</dependency>
```

## Assertion vs. Assumption

There are two main methods when using `Insist`.
They are `assertion()` and `assumption()` in the `Insist` static class.
The difference is that on validation failure that starts with the `assertion()` method an `AssertionFailedError` throwable will be thrown.
If the validation started with `assumption()`, on the other hand, a `TestAbortedException` throwable will be thrown.

## Stack trace example

`Insist` is designed to be used during test methods directly.
In that case there should not be a deep inheritance or call stack hierarchy rendering stack traces moot.
Regular assertion libraries leave the stack trace intact which is full of implementation details of the test runner itself when what failed was an expected condition.
Validity was designed to give better standard comparison messages for failed state checks and by removing the extraneous stack trace elements it leads to cleaner, less noisy errors.
This reduces the "cognitive overhead" when investigating test failures.
Note that due to the removal of stack traces, `Insist` should **only** be used during an actual test method, `Validity` should be used directly during production code or test frameworks to preserve the stack trace behavior needed for more complex debugging.

```java
@Test
public void testAsserts() {
    Insist.assertion().that("hello").startsWith("w");
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
    Insist.assertion("hello, world").that("hello").startsWith("w");
}
```

You would get an error like:

```java
java.lang.AssertionFailedError: Hello, world
    expected : t -> t.startsWith("w")
     subject : <hello>

	at com.redfin.example.FooTest.testAsserts(FooTest.java:41)
```
