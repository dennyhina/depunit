
import org.depunit.annotations.*;
import static java.lang.System.out;
import static org.junit.Assert.*;

public class UnitTest
	{
	String value;
	
	@AfterClass
	public void after()
		{
		out.println("After class");
		}
		
	@Test(
		cleanupMethod = "logout")
	public void login()
		{
		out.println("login");
		}
		
	@Test
	public void logout()
		{
		out.println("logout");
		}
		
	@Test(
		hardDependencyOn = { "login" })
	public void testOne()
		{
		out.println("testOne");
		}
		
	@Test(
		hardDependencyOn = { "login" })
	public void testTwo()
			throws Exception
		{
		out.println("testTwo");
		//throw new Exception("Error");
		assertTrue(false);
		}
		
	@Test(
		hardDependencyOn = { "login", "testTwo" })
	public void testThree()
		{
		out.println("testThree");
		}
		
		
	@BeforeClass
	public void before()
		{
		out.println("Before class");
		}
	}

