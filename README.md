# RxEspresso
Filling the gap between RxJava and Espresso



## Setup

#### Include as gradle dependency
TODO

#### Include source as library module
1. From project root in terminal run:

	```
git submodule add git@github.com:stablekernel/RxEspresso.git
  ```

2. Add dependency to this library module in your app module's build.gradle file:

	```
dependencies {
    ...
	androidTestCompile project(':RxEspresso')
}
	```

## Usage

1. Set the globa log level:

	```
RxEspresso.setLogLevel(LogLevel.DEBUG);
```

2. Increment and decrement the counter based on your design. The flexibility of this library means that you decide which Observable chains Espresso should wait for and which it should not. We chose to increment onSubscribe and decrement afterTerminate

	```
dataStore.getData()
     .subscribeOn(Schedulers.computation())
     .observeOn(AndroidSchedulers.mainThread())
     .doOnSubscribe(() -> RxEspresso.increment())
     .doAfterTerminate(() -> RxEspresso.decrement())
     .subscribe(// on Next);
```

3. Monitor the idle state. This is optional but RxEspresso exposes an `isIdleNow` method to track idle state. This is helpful in ensuring that monitored Observables have completed before a new test begins. Since any open streams after one test will leave the app in an indeterminate state for the next test, we chose to check idle state between tests and fail the whole suite if not idle:

	```
	public class BaseTest {
    	@After
	    public void tearDown() throws Exception {
        	// if there is anything still idling then future tests may fail
	        boolean idleNow = RxEspresso.isIdleNow();
    	    if (!idleNow) {
        	    String msg = "Test is over but RxEspresso is not idle. " +
            	        "Remaining tests may fail unexpectedly.";
	            Log.e("TESTING", msg);
    	        System.exit(-1);
        	}
	    }
	}
	```

## Better Usage

Since `doOnSubscribe` and `doAfterTerminate` are always used together, we use a follow [Dan Lew's pattern](http://blog.danlew.net/2015/03/02/dont-break-the-chain/) using a Transformer to better compose observable chains. We bundle 

```
public final class RxEspressoTransformer{

    private final Observable.Transformer transformer;

    public RxEspressoTransformer() {
        transformer = observable -> ((Observable) observable)
                .doOnSubscribe(() -> RxEspresso.increment())
                .doAfterTerminate(() -> RxEspresso.decrement());
    }

    public <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) transformer;
    }
}	
```

and then apply our Transformer in code:


```
RxEspressoTransformer rxEspressoTransformer = new RxEspressoTransformer();

dataStore.getData()
     .subscribeOn(Schedulers.computation())
     .observeOn(AndroidSchedulers.mainThread())
     .compose(rxEspressoTransformer.applySchedulers())
     .subscribe(// on Next);
```

However we are still leaking test code into production. Instead we define a `SchedulersInterface` and using dependency injection, supply *production* and *test* implementations. Only within the *test* implementation do we call into RxEspresso:


```
public interface SchedulersTransformer {
    <T> Observable.Transformer<T, T> applySchedulers();
}
```

```
public final class ProductionSchedulersTransformer implements SchedulersTransformer {

    private final Observable.Transformer schedulersTransformer;

    public ProductionSchedulersTransformer() {
        schedulersTransformer = observable -> ((Observable) observable)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) schedulersTransformer;
    }
}
```

```
public final class RxEspressoTransformer implements SchedulersTransformer {

    private final Observable.Transformer schedulersTransformer;

    public RxEspressoTransformer() {
        schedulersTransformer = observable -> ((Observable) observable)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> RxEspresso.increment())
                .doAfterTerminate(() -> RxEspresso.decrement());
    }

    @Override
    public <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) schedulersTransformer;
    }
}
```

Then in code we call our injected instance of `SchedulersTransformer`:

```
@Inject SchedulersTransformer schedulersTransformer;

dataStore.getData()
     .compose(schedulersTransformer.applySchedulers())
     .subscribe(// on Next);
```

	