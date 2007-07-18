
import org.depunit.annotations.*;
import org.depunit.RunContext;
import static java.lang.System.out;
import static org.junit.Assert.*;

public class LoginTest
	{
	private RunContext m_context;
	
	private String m_userName;
	private String m_password;
	
	public LoginTest(RunContext context)
		{
		m_context = context;
		m_userName = "Bob";
		m_password = "bobbyboy";
		}
		
	public void setUserName(String userName)
		{
		m_userName = userName;
		}
		
	public void setPassword(String password)
		{
		m_password = password;
		}
		
	public void setTest(int val)
		{
		}
		
	@Test(
		cleanupMethod = "logout")
	public void login()
		{
		out.println("    user = "+m_userName);
		out.println("    password = "+m_password);
		m_context.setParam("Login", this);
		assertTrue(true);
		}
		
	@Test
	public void logout()
		{
		}
	}
