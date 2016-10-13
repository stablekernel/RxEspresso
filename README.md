# RxEspresso
Filling the gap between RxJava and Espresso.

When using RxJava Observables in code-under-test, it is sometimes desireable to notify Espresso to idle until the observable stream completes. RxEspresso allows you to label some observables such that Espresso will wait for them to complete before proceeding.


## Bootstrapping
Just set the `testInstrumentationRunner` to `RxAndroidJUnitRunner` in your app module's build.gradle file:

```java
    defaultConfig {
        ...
        testInstrumentationRunner "com.stablekernel.rxespresso.RxAndroidJUnitRunner"
    }
```

## Include as gradle dependency
TODO

## Include source as library module
1. From project root in terminal run:

	```
	$ git submodule add git@github.com:stablekernel/RxEspresso.git
	```

2. Add dependency to this library module in your app module's build.gradle file:

	```
    dependencies {
        ...
        androidTestCompile project(':RxEspresso')
    }
	```

## Usage

Each time an Observale is subscribed to which Espresso should idle for, increment the counter:

```
RxIdlingResource.get().increment();
```

The counter will autmoatically decrement for each Observable which subscribed on the `main` thread. So there is no need to manually decrement the counter.

When the counter reaches zero, RxEspresso will allow Espresso to continue with the test.