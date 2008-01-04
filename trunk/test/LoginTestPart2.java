
import org.depunit.annotations.*;
import org.depunit.RunContext;
import static java.lang.System.out;

public class LoginTestPart2
	{
	private RunContext m_context;
	private String m_myData;
	
	public LoginTestPart2(RunContext context)
		{
		m_context = context;
		m_myData = "Not set";
		}
		
	public void setMyData(String data) { m_myData = data; }
		
	@Test(
		hardDependencyOn = { "LoginTest.login" } )
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
		hardDependencyOn = { "LoginTest.login" } )
	public void testThree()
		{
		//out.println("testThree");
		}
	}
