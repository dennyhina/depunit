# Introduction #

Reason for creating yet another Java unit test framework.  I know it is insane, there are already so many to choose from.  I admit I have only used two: JUnit and TestNG.  Both very good but, always lacking in my mind.  I'll start by telling what I didn't like about these two and if you can sympathize, maybe DepUnit is for you.

JUnit:  Annotations were a huge step in the right direction for making this one easier to use.  It always seemed wrong to me to have each test method executed on its own instance of the class.  If I wanted to share data among the tests it had to be placed statically on the object.  Each method on its own instance kind of takes me back to my procedural programming days.

When using JUnit I found my self creating a lot of setup and tear down code for my tests.  Usually the setup and tear down code could and should be tests as well.  I would resort to ordering my tests in the class in such a way so that the first tests were setup for the middle tests and then the last test were the tear down part.  For example, say you have a server you are testing.  Your code needs to log in, create a project, run some tests, delete the project and then log out.  Login, create project, could be setup and delete project and logout could be tear down methods or the could all be tests.  I created these ugly unit test classes where everything was static so each of the test methods could get to the context data.  My login would set some session handle.  The create project would set some project ID.  And so on.  I found very few of my unit tests to fit within the JUnit paradigm.  It would also be nice if JUnit would stop if a test failed so that all the rest of the tests that depended on the failed one wouldn't fail and clog up my log.  So I moved on to TestNG.

TestNG:  What a step in the right direction.  It would stop on an error.  It had dependencies.  But again I had issues.  I wanted to set soft dependencies (Basically A will always run after B weather A fails or not).  Also TestNG messes up the order of the tests in a class.  Messing with the order shouldn't bug me but it does.

Another thing that bugs me is parameters on test methods.  Parameters on a test method just do not seem right for some reason.  Parameters on test methods feels like a good patch on a bad hole.  Parameterization of test methods is just that, a patch and not the right solution.  Thinking about parameters on test methods and what the right solution is, lead me to realize unit tests need to be data driven.  Putty parameters on test methods is no the way to drive unit tests with data.


# Data Driven Testing #

There is always some aspect of data in everyones code.  If there were no input data of any kind the code would return the exact same result every time it ran.  If there were no input data to the code it could be written once, ran and then never be tested again, because its output will never change.  All unit tests use some kind of data to test against our code.  Because the variations of data can change drastically, shouldn't our tests be driven by that data.  Ideally the same code should be tested with a range of data to make sure some extreme value does not break the code.

To be data driven DepUnit uses the concept of TestBeans.  The test bean concept lets you set values on an object and then run various tests on those values.  Each test class in your unit tests represent a set of data that you wish to perform tests on.  Lets say you have a program that parses an input file and puts the data in a database.  For each file you have two tests.  The first test parses the header and the second test parses the body of the file. The class for these tests will look like this:
```
public class TestFileImport {
  private String m_file;

  public void setFile(String file) { m_file = file; }

  @Test
  public void parseHeader() {
    // Code to parse the header
    }

  @Test
  public void parseBody() {
    // Code to parse body
    }
  }
```

The test xml file looks like this
```
<suite name="test">
<run name="Repeat run">
  <classes>
    <class name="TestFileImport">
      <driver>
        <data>
          <value name="File">test_file_1</value>
        </data>
        <data>
          <value name="File">test_file_2</value>
        </data>
      </driver>
    </class>
  </classes>
</run>
</suite>
```

The above XML will run the tests located in the TestFileImport class twice, once for the file "test\_file\_1" and second for the file "test\_file\_2".

If you only wanted to run the tests once with one file you can abbreviate the xml like this:
```
<suite name="test">
<run name="Repeat run">
  <classes>
    <class name="TestFileImport">
      <value name="File">test_file_1</value>
    </class>
  </classes>
</run>
</suite>
```