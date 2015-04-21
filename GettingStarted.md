# Getting Started #

First off create a test class to contain your unit tests.  Just like in OOP place test methods that work on the same data in the same class.  Grouping tests by the data they work on will make adding a data driver to your test easier down the road.

Download the depunit.jar file then create the following class:
```
import org.depunit.annotations.*;
import static org.junit.Assert.*;

public class MyFirstTestClass
  {
  @Test
  public void testOne()
    {
    System.out.println("Test one, hurray!");
    assertTrue(true);
    }
  }
```

Notice that DepUnit includes the Assert class from JUnit (it was so good, why rewrite it?).

Now run the test:
```
>java -cp .;depunit.jar org.depunit.DepUnit -c MyFirstTestClass
```

Now lets put it inside an XML file so it will be easier to manage as the project gets larger.  Create a file unittest.xml
```
<suite name="My Test Suite">
  <run name="My first run">
    <classes>
      <class name="MyFirstTestClass"/>
    </classes>
  </run>
</suite>
```

Now run the xml file:
```
>java -cp .;depunit.jar org.depunit.DepUnit -x unittest.xml
```

Now that you have your feet wet with a simple example try adding more tests and have a look at the following pages for more in depth help.

CommandLine - Other command line options you can use.

[XMLFormat](XMLFormat.md) - Format of the XML file and other things you can do with it.

[Dependencies](Dependencies.md) - The true power of DepUnit.

DataDriver - Run the same tests with different data.