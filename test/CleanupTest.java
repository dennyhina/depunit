
import org.depunit.annotations.*;
import static java.lang.System.out;
import static org.junit.Assert.*;

/**
This tests the adding of soft dependencies for the cleanup methods
*/
public class CleanupTest
	{
	@Test(
		cleanupMethod = "logout" )
	public void login()
		{
		}
		
	@Test
	public void logout()
		{
		}
	
	@Test(
		hardDependencyOn = { "login" },
		cleanupMethod = "outerCleanup" )
	public void outerSetup()
		{
		}
		
	@Test
	public void outerCleanup()
		{
		}
		
		
	@Test(
		hardDependencyOn = { "outerSetup" },
		cleanupMethod = "innerCleanup")
	public void innerSetup()
		{
		}
		
	@Test
	public void innerCleanup()
		{
		}
	}

