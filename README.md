# RxEspresso
Filling the gap between RxJava and Espresso

## Include source as library module
From project root:
`git submodule add https://github.com/rosshambrick/RxEspresso.git`

In build.gradle:
```
    defaultConfig {
        ...
        testInstrumentationRunner "com.rosshambrick.rxespresso.RxAndroidJUnitRunner"
    }

    dependencies {
        ...
        androidTestCompile project(':rxespresso')
    }
```

## Include as gradle dependency
TODO

