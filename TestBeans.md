# Test Beans #
DepUnit uses the concept of a test bean. (I searched and found this not to be a known concept so, I'm making it one ;)

A test bean is really made of two things:
  1. Private members that can be set via the standard set method.
  1. Test methods.  Test methods are zero parameter methods that perform some test on the data held in the private members.  These methods are denoted by the @Test annotation.

Running tests contained within TestBeans starts with DepUnit constructing the object.  DepUnit then sets the properties on the TestBean with the appropriate set methods.  DepUnit calls the test methods in order according to the dependencies set on the methods.

With this setup it is easy to test the same code with different data.  Data drivers can be setup to run the same TestBean's tests with different data.  Here is an example test.xml file:
```
<suite name="example">
 <run name="Create Project Tests">
  <classes>
   <class name="Project">
    <driver>
     <data>
      <value name="ProjectName">my project</value>
     </data>
     <data>
      <value name="ProjectName">html test project</value>
      <value name="ProjectFile">test/test.html</value>
     </data>
    </driver>
   </class>
  </classes>
 </run>
</suite>
```

This XML file repeats the test found within the Project class twice.  Once for the project named "my project" and the second for the project named "html test project".  Notice that in the second run it will also set the value for the project file to "test/test.html".  This is one advantage of using test beans vs parameterized test methods.  You set only what you want to.  If you don't set it, it will use the default values or the once set by the previous test.  (Right now it will reuse the same instance of the object for each test run.  I'm toying with the idea of an option to tell it to create a new instance)