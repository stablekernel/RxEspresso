/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stablekernel.rxespresso;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.idling.CountingIdlingResource;

/**
 * Wrapper around {@link CountingIdlingResource} which easily creates object and exposes increment/decrement/isIdleNow methods
 */

public final class RxEspresso {
    private static final String TAG = "RxEspresso";
    private static int LOG_LEVEL = LogLevel.NONE;
    private static RxEspresso INSTANCE;

    private CountingIdlingResource countingIdlingResource;

    public static void setLogLevel(@LogLevel int logLevel) {
        RxEspresso.LOG_LEVEL = logLevel;
    }

    public static void increment() {
        get().countingIdlingResource.increment();
    }

    public static void decrement() {
        get().countingIdlingResource.decrement();
    }

    public static boolean isIdleNow() {
        return get().countingIdlingResource.isIdleNow();
    }

    private RxEspresso() {

        boolean debug = false;
        if (LOG_LEVEL == LogLevel.DEBUG) {
            debug = true;
        }
        countingIdlingResource = new CountingIdlingResource(TAG, debug);
        Espresso.registerIdlingResources(countingIdlingResource);
    }

    private static RxEspresso get() {
        if (INSTANCE == null) {
            INSTANCE = new RxEspresso();
        }
        return INSTANCE;
    }

}
