# Terminology #

In looking around at different unit test frameworks there appears to be no standard for terms like "test suite", "test case" or even "test" for that matter.  So here I'm going to define what they mean in DepUnit.

Without further ado here is my list of terms
  * **Test**  A test is a single method on a class that is marked with the @Test annotation.
  * **Run** or **Test Run**  A run is a group of tests that are all ran together.  A run represents a group of tests that are dependent upon each other for data, connections, etc.
  * **Suite**  A suite is a set of runs.