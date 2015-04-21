# Passing data between classes #

The RunContext class provides a mechanism by which data can be passed from one class to another within the same run.  The RunContext contains a Map<String, Object>.  The RunContext provides two methods setParam and getParam to set and retrieve objects from the internal map.

To obtain the RunContext simply define the constructor of you class like so:
```
public MyTestClass(RunContext context)
  {
  //Save context to member variable
  }
```

DepUnit will detect whether your constructor takes a RunContext as a parameter and if it does it will hand it to your class when it is constructed for the current run.

During a test you can set data on the RunContext that can be used by later tests.  A unit test, called dbConnect, can create a connection to a database and then place that connection on the RunContext.  Other tests that have a hard [dependency](http://code.google.com/p/depunit/wiki/Dependencies) on dbConnect can pull off the connection and use it for their tests.  In this case it would also be a good idea to add a [cleanup](http://code.google.com/p/depunit/wiki/CleanupMethod) method for dbConnect that disconnects from the database.

## Update ##
The run context just got easier to use.  Downstream consumers of context data need only provide set methods that match the data on the context.  Lets look at an example:
```
public class DBSetup
  {
  private RunContext m_context;
  public DBSetup(RunContext context)
    {
    m_context = context;
    }

  @Test
  public openConnection()
    {
    Connection con = //Code that sets up the db connection
    m_context.setParam("MyConnection", con);
    }
  }


public class MyDBTests
  {
  private Connection m_connection;
  //This will get called and set from the context
  public void setMyConnection(Connection con)
    {
    m_connection = con;
    }

  @Test(hardDependencyOn = { "DBSetup.openConnection" })
  public myTest()
    {
    //Do something with m_connection
    }
  }
```

In the DBSetup class the run context is obtained and saved in the constructor.  The _openConnection_ method sets the connection on the context.  Later when the MyDBTests class methods need to consume the connection it is set automatically.  The run context checks for any set method that matches the name and data that is in the context.