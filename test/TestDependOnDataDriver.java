
import org.depunit.annotations.*;
import org.depunit.RunContext;
import static java.lang.System.out;

public class TestDependOnDataDriver
	{
	private RunContext m_context;
	private String m_myData;
	
	public TestDependOnDataDriver(RunContext context)
		{
		m_context = context;
		m_myData = "Not set";
		}
		
	public void setMyData(String data) { m_myData = data; }
		
	@Test(
		hardDependencyOn = { "RepeatCleanup.testTwo" } )
	public void myTest()
		{
		//out.println("testTwo");
		}
	}
