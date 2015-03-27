/**
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

package com.rosshambrick.rxespresso;

/**
 * Facade to simplify the configuration of RxEspresso
 */

public class RxEspresso {
    static LogLevel LOG_LEVEL = LogLevel.NONE;

    /***
     * Enable logging to troubleshoot the RxIdlingResource tracking.
     *
     * Filter on TAG: "RxIdlingResource" and you can view the start and finish of each
     * subscription.  Once you've identified an Observable that never completes, enabling
     * stack traces will help to identify the origin of that onSubscribeStart()
     *
     * @param logLevel
     */
    public static void setLogLevel(LogLevel logLevel) {
        LOG_LEVEL = logLevel;
    }
}
