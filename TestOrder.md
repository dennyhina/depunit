# Test Ordering #

Without any dependencies defined DepUnit will run the tests in the order in which they show up in the class:
```
public class TestClass
  {
  @Test
  public void testOne()
    ...

  @Test
  public void testTwo()
    ...

  @Test
  public void testThree()
    ...
  }
```

The above tests will run in this order
  1. testOne
  1. testTwo
  1. testThree

The order can be changed by adding hard or soft dependencies to each test.  Test ordering is very deterministic.
