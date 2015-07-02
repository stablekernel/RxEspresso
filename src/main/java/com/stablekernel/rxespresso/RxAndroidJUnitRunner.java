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

package com.stablekernel.rxespresso;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import rx.plugins.RxJavaPlugins;

/**
 * This runner creates an Observable hook and registers it as
 * an Espresso idling resource so that your Espresso tests
 * will wait for async Rx subscriptions to finish before
 * progressing.
 *
 * USAGE: Set this custom runner in your build.gradle file
 *
 *   defaultConfig {
 *       ...
 *       testInstrumentationRunner "com.stablekernel.rxespresso.RxAndroidJUnitRunner"
 *   }
 */
public class RxAndroidJUnitRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle arguments) {
        RxJavaPlugins.getInstance().registerObservableExecutionHook(RxIdlingResource.get());

        super.onCreate(arguments);
    }
}
