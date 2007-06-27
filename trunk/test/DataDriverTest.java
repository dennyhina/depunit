import org.depunit.annotations.*;


public class DataDriverTest
	{
	private String m_data;
	
	public DataDriverTest()
		{
		}
		
	public void setData(String data) { m_data = data; }
		
	@Test
	public void testDataDriver()
		{
		System.out.println(m_data);
		}
	}
