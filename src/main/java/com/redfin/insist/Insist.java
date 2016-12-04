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

/**
 * todo
 */
public final class Insist {

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Constants
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
     * Cache a re-usable instance without a custom message prefix for improved
     * performance. When a custom message is desired a new instance is
     * required.
     */

    private static final AssertionFactory NO_MESSAGE_ASSERTION_FACTORY = new AssertionFactory(null);
    private static final AssumptionFactory NO_MESSAGE_ASSUMPTION_FACTORY = new AssumptionFactory(null);

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Static Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * todo
     *
     * @return
     */
    public static AssertionFactory assertion() {
        return NO_MESSAGE_ASSERTION_FACTORY;
    }

    /**
     * todo
     *
     * @param message
     *
     * @return
     */
    public static AssertionFactory assertion(String message) {
        if (null == message) {
            return NO_MESSAGE_ASSERTION_FACTORY;
        }
        return new AssertionFactory(message);
    }

    /**
     * todo
     *
     * @return
     */
    public static AssumptionFactory assumption() {
        return NO_MESSAGE_ASSUMPTION_FACTORY;
    }

    /**
     * todo
     *
     * @param message
     *
     * @return
     */
    public static AssumptionFactory assumption(String message) {
        if (null == message) {
            return NO_MESSAGE_ASSUMPTION_FACTORY;
        }
        return new AssumptionFactory(message);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Instance Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Insist() {
        throw new AssertionError("Cannot instantiate the static class Insist");
    }
}
