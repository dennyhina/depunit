import org.depunit.annotations.*;
import org.depunit.RunContext;
import static java.lang.System.out;
import static org.junit.Assert.*;

public class Project
	{
	public void setProjectName(String name)
		{
		}
		
	public void setLogin(LoginTest login)
		{
		System.out.println("LOGIN IS NOW SET");
		}
	
	@Test(
		hardDependencyOn = { "LoginTest.login" })
	public void getTestParams()
		{
		}
	
	@Test(
		cleanupMethod = "deleteCompany",
		hardDependencyOn = { "LoginTest.login" })
	public void createCompany()
		{
		}
		
	@Test
	public void plainTestMethod()
		{
		}
		
	@Test
	public void deleteCompany()
		{
		}
		
	@Test(
		cleanupMethod = "deleteProject",
		hardDependencyOn = { "LoginTest.login", "getTestParams", "createCompany" })
	public void createProject()
		{
		}
		
	@Test
	public void deleteProject()
		{
		}
	}
