# RxEspresso
Filling the gap between RxJava and Espresso

## Bootstrapping
Just set the `testInstrumentationRunner` to `RxAndroidJUnitRunner` in your app module's build.gradle file:

```java
    defaultConfig {
        ...
        testInstrumentationRunner "com.rosshambrick.rxespresso.RxAndroidJUnitRunner"
    }
```

## Include as gradle dependency
TODO

## Include source as library module
1. From project root in terminal run:
`git submodule add https://github.com/rosshambrick/RxEspresso.git`

2. Add dependency to this library module in your app module's build.gradle file:

```java
    dependencies {
        ...
        androidTestCompile project(':rxespresso')
    }
```

## Usage
If you typically use "cold" Observables or short lived "hot" Observables, you shouldn't have to do anything special to make this work in your project.

There *is* **one big exception**. All observables must complete before Espresso will continue.  (I'm still trying to figure out a solution to this).  Below are some of the common use cases that will cause problems:

* You cannot use RxJava in an "event bus" style where you have an Observable that forever remains "hot" to serve up events to interested subscribers.  If you are using RxJava in this manner and want to use Espresso, you will need to migrate to another event bus option.  My preference is using [EventBus](https://github.com/greenrobot/EventBus)
* You will also run into issues if you have Observables running outside the scope of the UI being tested, such as in an Android Service.  The specifics of your usage will determine the level of impact.  Possibly disabling the services when being tested by Espresso could solve the issue.

## Tips
If you do have a situtation where the tests aren't running as expected due to a never-finishing Observable, you can:

1. Enable logging by calling `RxEspresso.setLogLevel(LogLevel.DEBUG)` somewhere in your test setup, and filter you logcat based on the TAG: `RxIdlingResource`.  By looking at the Object reference IDs, you can identify any that have started but not completed.

2. Once you've identified one that never completes, you can enable logging with stack traces to see where this Observer subscription originates from by calling `RxEspresso.setLogLevel(LogLevel.VERBOSE)`

Better support for identifying these blocking Observables will be added in a future version of RxEspresso. 