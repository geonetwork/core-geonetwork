# Testing

GeoNetwork is a standard Java project, primarily using on JUnit for testing. There is am important separation between unit tests and integration tests.

## Unit Tests

In Geonetwork *Unit Tests* should be very very quick to execute and not start up any subsystems of the application in order to keep
the execution time of the unit tests very short.
    
Efficient Unit tests do not require super classes and any assistance methods can be static
imports, for example statically ``importing org.junit.Assert``` or ``org.junit.Assume``` or ``org.fao.geonet.Assert``.

## Integration Tests

*Integration Tests* typically start much or all of GeoNetwork as part of the test and will take longer to run than
a unit test. However, even though the tests take longer they should still be implemented in such a way to be as efficient as possible.

Starting GeoNetwork in a way that isolates each integration test from each other integration test is non-trivial.  Because of this
there are `abstract` super classes to assist with this.  Many modules have module specific Abstract classes.  For example `domain`, `core`, `harvesters` and `services` modules all have module specific super classes that need to be used.  (`harvesting` has 2 superclasses depending on what is to be tested.)
    
The easiest way to learn how to implement an integration test is to search for other integration tests in the same module as the class
you want to test.

The following list provides a few tips:

* *IMPORTANT*: All Integrations tests *must* end in `IntegrationTest`.  The build system assumes all tests ending in `IntegrationTest` are
  an integration test and runs them in a build phase after unit tests.  All other tests are assumed to be unit tests.

* Prefer Unit Tests over Integration Tests because they are faster.

* Search the current module for an `IntegrationTest` to find tests to model your integration test against

* Integration tests are used for testing:

  * Services: If the service already exists and you quick need to write a test to debug/fix its behaviour.
    If you are writing a new service it is better to use Mockito to mock the dependencies of the service so the test is
    a unit test.
  * Harvesters
  * A behaviour that crosses much of the full system

* **org.fao.geonet.utils.GeonetHttpRequestFactory*: When making Http requests you should use `org.fao.geonet.utils.GeonetHttpRequestFactory` instead
  of directly using `HttpClient`.  This is because there are mock instances of `org.fao.geonet.utils.GeonetHttpRequestFactory` that can
  be used to mock responses when performing tests.

* Integration tests are disabled by default. Use the `it` maven profile to use them.

## Caveats when using Windows

Some unit tests are designed to expect LF line seperators, which makes them fail on Windows.
For these Tests to run correctly, you should set up the project in the [WSL](https://learn.microsoft.com/en-us/windows/wsl/install).
Once you have installed the WSL, make sure to clone the repo into the WSL filesystem and install the correct java and maven.

If you are using IntelliJ, you can also use the WSL filesystem as the project root.
See the [IntelliJ Docs](https://www.jetbrains.com/help/idea/how-to-use-wsl-development-environment-in-product.html#open-a-project-in-wsl) regarding WSL integration and the [IntelliJ IDE](INTELLIJ.md) instructions for general setup.
