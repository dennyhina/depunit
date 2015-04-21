# Test Database Queries #

In one of my projects I have a bunch of SQL queries that are saved inside of an XML file.  The database tends to change a lot more then it should so I created a unit test that runs all the queries against the database to make sure they still work.

I first made a Database test class that connects and disconnects to the database.

```
import org.depunit.annotations.*;
import org.depunit.*;
import org.postgresql.jdbc3.*;
import java.sql.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.apache.xerces.jaxp.*;

public class Database
	{
	private String m_server;
	private String m_database;
	private String m_user;
	private String m_password;
	
	private Connection m_connection;
	
	public Database(RunContext context)
			throws Exception
		{
		//A bunch of code that got the database info from a config file
		
		//Set the this object on the context
		context.setParam("Database", this);
		}

	//Returns the database connection
	public Connection getConnection() { return (m_connection); }
		
	@Test(
		cleanupMethod = "disconnect")
	public void connect()
			throws SQLException  //If the test throws an exception it fails
		{
		Jdbc3PoolingDataSource source = new Jdbc3PoolingDataSource();
		source.setDataSourceName("Postgres LingoPoint DataSource");
		source.setServerName(m_server);
		source.setDatabaseName(m_database);
		source.setUser(m_user);
		source.setPassword(m_password);
		
		//Attemp to get a connection to validate database parameters
		m_connection = source.getConnection();
		}
		
	@Test
	public void disconnect()
			throws SQLException
		{
		m_connection.close();
		}
	}
```

The next step I made a QueryTest class that had one test method.  The test method runs the query against the database.

```
import org.depunit.annotations.*;
import org.depunit.*;
import java.sql.*;
import java.util.*;

public class QueryTest
	{
	private String m_queryName;
	private String m_query;
	
	private Database m_database;
	
	public QueryTest(RunContext context)
		{
		//Get the Database object off of the context.
		m_database = (Database)context.getParam("Database");
		}
		
	//Set methods for each query
	public void setQueryName(String name) { m_queryName = name; }
	public void setQuery(String query) { m_query = query; }
	
	@Test(
		hardDependencyOn = { "Database.connect" } )
	public void testQuery()
			throws SQLException
		{
		System.out.println("    "+m_queryName);
		Connection c = m_database.getConnection();
		
		PreparedStatement ps = c.prepareStatement(m_query);
		
		ps.execute();		
		ps.close();
		}
	}
```

I could have at this point just made the testQuery method read all of the queries out of the XML file and run each one.  The problem with running all the tests in one method is that they show up as one test in the report.  It would be nice to show each query as a separate test that is reported individually.  To do this we add a data driver to the unit test XML file.  Here is the run tag in the XML:
```
<run name="Query tests">
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

One thing to note here is that you can pass values to a data drive in the same way that you pass data to a test class.  Here I pass the name of the query file as a parameter.

Here is the code for the data driver class:
```
import org.depunit.*;
import org.apache.xerces.jaxp.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class QueryDataDriver implements DataDriver
	{
	private static final String QUERY_NAME = "QueryName";
	private static final String QUERY = "Query";
	private static final String INPUT_PARAMS = "InputParams";
	
	private List<Map<String, ? extends Object>> m_queries;
	private Iterator<Map<String, ? extends Object>> m_iterator;
	
	public QueryDataDriver()
		{
		}
	
	public void setQueryFile(String file)
			throws Exception
		{
		m_queries = new ArrayList<Map<String, ? extends Object>>();
		
		InputStream queryFile = new FileInputStream(file);
		
		DocumentBuilderFactoryImpl dbFactory = new DocumentBuilderFactoryImpl();
		DocumentBuilder db = dbFactory.newDocumentBuilder();
		Document xmldoc = db.parse(queryFile);
		
		NodeList nl = xmldoc.getElementsByTagName("query");
		
		for (int I = 0; I < nl.getLength(); I++)
			{
			Map<String, Object> queryData = new HashMap<String, Object>();
			Element e = (Element)nl.item(I);
			
			queryData.put(QUERY_NAME, e.getAttribute("name"));
			queryData.put(QUERY, e.getAttribute("sql"));
			
			m_queries.add(queryData);
			}
			
		queryFile.close();
		
		reset();
		}
		
	
	/*DataDriver*/
	public void reset()
		{
		m_iterator = m_queries.iterator();
		}
	
	/*DataDriver*/
	public boolean hasNextDataSet()
		{
		return (m_iterator.hasNext());
		}
		
	/*DataDriver*/
	public Map<String, ? extends Object> getNextDataSet()
		{
		return (m_iterator.next());
		}
	}
```

See the DataDriver page for more information on how data drivers work.

The data driver returns a Map for each run of the test.  The map's key is the name of the property and the value is - well - the value.