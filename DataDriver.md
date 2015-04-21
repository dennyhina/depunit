# Data Drivers #

Data drivers let you run the same unit test with different data each time.  Quite often a new unit test is not testing new code but testing new data with old code.  A data driver is a simple means by which you can run existing tests with new data.

Data drivers work on by utilizing the TestBeans idea.  For example lets look at a test that tests the login method of some system.  Here is the run defined in the XML:
```
<run name="First Run">
  <classes>
    <class name="LoginTest">
      <value name="UserName">Sammy</value>
      <value name="Password">secret</value>
    </class>
  </classes>
</run>
```

The data driver in this example is a bit hidden.  Before running the tests in the LoginTest class the methods setUserName and setPassword are called with the associated values.  Now lets run the login test with another user name to test it out.
```
<run name="First Run">
  <classes>
    <class name="LoginTest">
      <driver>
        <data>
          <value name="UserName">Sammy</value>
          <value name="Password">secret</value>
        </data>
        <data>
          <value name="UserName">Bob</value>
          <value name="Password">bobsecret</value>
        </data>
      <driver>
    </class>
  </classes>
</run>
```

Now the data driver is a little more apparent.  DepUnit runs the tests in the LoginTest class twice.  The first time it sets the user name and password to Sammy and secret respectively.  The second time through it sets the user name and password to Bob and bobsecret.

If you want to write your own data driver you can.  Simply create a class that implements the DataDriver interface and then specify it in the XML like so:
```
<run name="First Run">
  <classes>
    <class name="LoginTest">
      <driver class="MyDataDriver>
        <value name="MyProperty">MyValue</value>
      <driver>
    </class>
  </classes>
</run>
```

The above XML also shows how you can set properties on the data driver class before it is used.


## Data Drivers and Hard Dependencies ##

Data drivers effect hard dependencies.  Say we have tests A, B and C.  B and C have hard dependencies on A.  Now if A has a data driver that runs A three times, B and C will be ran three times as well.  The resulting run will look like this (A,B,C, A,B,C, A,B,C)

This may seem a little odd at first but it makes better sense if we apply it to an example.  Lets say instead of A, B and C we have Login, UpdateAccount, UpdateStatus.  UpdateAccount and UpdateStatus will only work if the Login test works; therefor they have a hard dependency on Login.  Now we put a data driver on the Login test to test two users with different rights on the system.  Logically we need to run the UpdateAccount and UpdateStatus tests for each login that we use.  This is why the hard dependencies are repeated for each run of the data driver.

## Nesting Data Drivers ##

Data drivers can also be nested.  Lets say we have tests A, B and C.  C has a hard dependency on B and B has a hard dependency on A.  Now if both A and B have a data driver with two sets of data each the resulting run would look like this ( A,B,C,B,C, A,B,C,B,C).  For each iteration of the A test the data driver for B is reset.