package com.rosshambrick.rxespresso;

import android.os.Handler;
import android.os.Looper;
import android.support.test.espresso.IdlingResource;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Subscription;
import rx.plugins.RxJavaObservableExecutionHook;

public class RxIdlingResource extends RxJavaObservableExecutionHook implements IdlingResource {
    public static final String TAG = "RxIdlingResource";
    private static final AtomicInteger subscriptions = new AtomicInteger(0);
    private static boolean LOGGING = false;

    private ResourceCallback resourceCallback;
    private final Handler mainHandler;

    public RxIdlingResource() {
        Looper mainLooper = Looper.getMainLooper();
        mainHandler = new Handler(mainLooper);
    }

    public static void enableLogging(boolean enable) {
        LOGGING = enable;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public boolean isIdleNow() {
        int currentSubscriptionCount = subscriptions.get();
        if (LOGGING) Log.d(TAG, "currentSubscriptionCount: " + currentSubscriptionCount);
        boolean isIdle = currentSubscriptionCount <= 0;
        Log.d(TAG, "isIdleNow: " + isIdle);
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        if (LOGGING) Log.d(TAG, "registerIdleTransitionCallback");
        this.resourceCallback = resourceCallback;
    }

    @Override
    public <T> Observable.OnSubscribe<T> onSubscribeStart(Observable<? extends T> observableInstance, Observable.OnSubscribe<T> onSubscribe) {
        int i = subscriptions.incrementAndGet();
        if (LOGGING) Log.d(TAG, "onSubscribeStart: " + i);
        return onSubscribe;
    }

    @Override
    public <T> Subscription onSubscribeReturn(Subscription subscription) {
        if (LOGGING) Log.d(TAG, "onSubscribeReturn");
        onFinalized();
        return subscription;
    }

    @Override
    public <T> Throwable onSubscribeError(Throwable e) {
        if (LOGGING) Log.d(TAG, "onSubscribeError");
        onFinalized();
        return e;
    }

    private void onFinalized() {
        int i = subscriptions.decrementAndGet();
        if (LOGGING) Log.d(TAG, "onFinalized: " + i);

        if (i == 0) {
            //postDelayed needed to prevent race conditions
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resourceCallback.onTransitionToIdle();
                }
            }, 100);
        }
    }
}
