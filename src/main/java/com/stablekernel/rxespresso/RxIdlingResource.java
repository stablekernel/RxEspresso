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
import android.support.test.espresso.IdlingResource;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscriber;
import rx.plugins.RxJavaObservableExecutionHook;

import static com.stablekernel.rxespresso.LogLevel.DEBUG;
import static com.stablekernel.rxespresso.LogLevel.VERBOSE;


/**
 * Provides the hooks for both RxJava and Espresso so that Espresso knows when to wait
 * until RxJava subscriptions have completed.
 */

public final class RxIdlingResource extends RxJavaObservableExecutionHook implements IdlingResource {
    public static final String TAG = "RxIdlingResource";

    static int LOG_LEVEL = LogLevel.NONE;

    // map of OnSubscribes and their initializing stack trace (available from Throwable)
    private HashMap<WeakReference<Observable.OnSubscribe>, Info> onSubscribeList = new HashMap<>();

    private final AtomicInteger subscriptions = new AtomicInteger(0);

    private static RxIdlingResource INSTANCE;

    private ResourceCallback resourceCallback;

    private RxIdlingResource() {
        //private
    }

    public static RxIdlingResource get() {
        if (INSTANCE == null) {
            INSTANCE = new RxIdlingResource();
            Espresso.registerIdlingResources(INSTANCE);
        }
        return INSTANCE;
    }

    /* ======================== */
    /* IdlingResource Overrides */
    /* ======================== */

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        int activeSubscriptionCount = subscriptions.get();
        boolean isIdle = activeSubscriptionCount == 0;

        if (LOG_LEVEL >= DEBUG) {
            Log.d(TAG, "activeSubscriptionCount: " + activeSubscriptionCount);
            Log.d(TAG, "isIdleNow: " + isIdle);
        }

        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        if (LOG_LEVEL >= DEBUG) {
            Log.d(TAG, "registerIdleTransitionCallback");
        }
        this.resourceCallback = resourceCallback;
    }

    /* ======================================= */
    /* RxJavaObservableExecutionHook Overrides */
    /* ======================================= */

    @Override
    public <T> Observable.OnSubscribe<T> onSubscribeStart(Observable<? extends T> observableInstance,
                                                          final Observable.OnSubscribe<T> onSubscribe) {

        int activeSubscriptionCount = subscriptions.incrementAndGet();

        String currentThreadName = Thread.currentThread().getName();
        Info info = new Info(new Throwable(), currentThreadName);
        onSubscribeList.put(new WeakReference<Observable.OnSubscribe>(onSubscribe), info);

        if (LOG_LEVEL >= DEBUG) {
            Log.d(TAG, "Subscription started. Active subscription count is now: " + activeSubscriptionCount);
        }

        return new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                onSubscribe.call(new Subscriber<T>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                        onFinally(onSubscribe, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                        onFinally(onSubscribe, "onError");
                    }

                    @Override
                    public void onNext(T t) {
                        subscriber.onNext(t);
                    }
                });
            }
        };
    }

    private <T> void onFinally(Observable.OnSubscribe<T> onSubscribe, final String finalizeCaller) {
        int activeSubscriptionCount = subscriptions.decrementAndGet();
        removeOnSubscribe(onSubscribe);

        if (LOG_LEVEL >= DEBUG) {
            Log.d(TAG, "Subscription finished because: " + finalizeCaller + ". Active subscription count is now: " + activeSubscriptionCount);

            if (LOG_LEVEL >= VERBOSE) {
                // remaining subscriptions:
                Log.d(TAG, ">>>>>>> remaining subscriptions ");
                printStackTraces();
                Log.d(TAG, "<<<<<<<<");
            }
        }
        if (activeSubscriptionCount == 0) {
            Log.d(TAG, "onTransitionToIdle");
            resourceCallback.onTransitionToIdle();
        }
    }

    private void removeOnSubscribe(Observable.OnSubscribe reference) {
        for (WeakReference<Observable.OnSubscribe> weakRef : onSubscribeList.keySet()) {
            if (weakRef.get() == reference) {
                onSubscribeList.remove(weakRef);
                return;
            }
        }
    }

    private void printStackTraces() {
        for (WeakReference<Observable.OnSubscribe> key : onSubscribeList.keySet()) {
            Observable.OnSubscribe onSubscribe = key.get();
            if (onSubscribe != null) {
            Info info = onSubscribeList.get(key);
                Log.d(TAG, onSubscribe.toString() + " on thread " + info.getThreadName(), info.getThrowable());
            }
        }
    }

    private class Info {
        private Throwable throwable;
        private String threadName;

        public Info(Throwable throwable, String threadName) {
            this.throwable = throwable;
            this.threadName = threadName;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public String getThreadName() {
            return threadName;
        }
    }
}
