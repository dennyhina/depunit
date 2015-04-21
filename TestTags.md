# Test Tagging #

What is test tagging?  If you have ever tagged email in gmail then you know how this works.  Here is an example of test tagging
```
<run name="Query tests">
  <tag>offline</tag>
  <classes>
    <class name="Database"/>
    <class name="QueryTest">
      <driver class="QueryDataDriver">
        <value name="QueryFile">inf/WEB-INF/queries.xml</value>
      </driver>
    </class>
  </classes>
</run>
```

This run is tagged with the word "offline".  A tag can be anything you want, to DepUnit it is just a string.  A test run can be tagged with as many tags as you like.  Just specify which tags you want to run when you launch DepUnit and only those unit tests with the specified tags will be ran.