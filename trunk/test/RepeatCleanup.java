
import org.depunit.annotations.*;
import org.depunit.RunContext;
import static java.lang.System.out;

public class RepeatCleanup
	{
	private RunContext m_context;
	private String m_myData;
	
	public RepeatCleanup(RunContext context)
		{
		m_context = context;
		m_myData = "Not set";
		}
		
	public void setMyData(String data) { m_myData = data; }
		
	@Test(
		cleanupMethod = "testThree"
		)
	public void testOne()
		{
		//out.println("testOne");
		}
		
	@Test(
		hardDependencyOn = { "testOne" } )
	public void testTwo()
		{
		//out.println("testTwo");
		}
		
	@Test(
		hardDependencyOn = { "testTwo" } )
	public void afterTestTwo()
		{
		}
		
	@Test
		//( hardDependencyOn = { "testOne" } )
	public void testThree()
		{
		//out.println("testThree");
		}
	}
