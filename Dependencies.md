# Dependencies #

Dependencies between unit tests are the core of DepUnit.  Dependencies are broken into two types: Soft and Hard.  Dependencies can be to methods of the same class or other classes.


## Soft Dependencies ##

How are they declared:
```
@Test(softDependencyOn = { "localMethod", "Class.method" })
```

Soft dependencies are really just used to ensure proper ordering of your tests.  DepUnit will ensure that the tests listed in the softDependencyOn array are ran before the current one.  The test method will always run regardless if the dependency tests pass or fail.


## Hard Dependencies ##

How they are declared:
```
@Test(hardDependencyOn = { "localMethod", "Class.method" })
```

Hard dependencies help DepUnit to decide when to run a test as well as if to run a test.  For example lets take the following test method declaration:
```
@Test(hardDependencyOn = { "A" })
public void B()
```

Test B has a hard dependency on test A.  Test B will always run after test A but only if test A succeeds.  If for some reason test A fails test B will not run.

Hard dependencies also effect the number of times a test is ran if there is a DataDriver involved.


## Dependency Lookup ##

When trying to find a dependency, DepUnit looks for the method in the following order:
  1. Look for dependency as a method on the local class (this includes inherited methods)
  1. Look for dependency as a partial path (className.methodName) in the current package list.  The current package list is the packages that make up the test class package and any test classes that it may inherit from.
  1. Look for dependency as a full path (packageName.className.methodName)