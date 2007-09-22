
import org.depunit.annotations.*;
import org.depunit.RunContext;
import static java.lang.System.out;
import static org.junit.Assert.*;

public class LoginTest
	{
	private RunContext m_context;
	
	private String m_userName;
	private String m_password;
	private boolean m_fail;
	
	public LoginTest(RunContext context)
		{
		m_context = context;
		m_userName = "Bob";
		m_password = "bobbyboy";
		m_fail = false;
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
		
	public void setFail(boolean fail)
		{
		m_fail = fail;
		}
		
	@Test(
		cleanupMethod = "logout")
	public void login()
		{
		out.println("    user = "+m_userName);
		out.println("    password = "+m_password);
		m_context.setParam("Login", this);
		
		assertTrue(!m_fail);
		}
		
	@Test
	public void logout()
		{
		}
	}
