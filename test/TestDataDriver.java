
import org.depunit.*;
import java.util.*;

public class TestDataDriver extends DataDriver
	{
	private String m_testValue;
	private List<String> m_dataSet;
	private Iterator<String> m_dataSetIterator;
	
	public TestDataDriver()
		{
		m_testValue = "Default Value";
		m_dataSet = new ArrayList<String>();
		m_dataSet.add("One");
		m_dataSet.add("Two");
		m_dataSet.add("Three");
		
		m_dataSetIterator = m_dataSet.iterator();
		}
		
	public void setTestValue(String value) { m_testValue = value; }
		
	public void reset()
		{
		m_dataSetIterator = m_dataSet.iterator();
		}
		
	public boolean hasNextDataSet()
		{
		return (m_dataSetIterator.hasNext());
		}
		
	public Map<String, ? extends Object> getNextDataSet()
		{
		HashMap<String, String> ret = new HashMap<String, String>();
		ret.put("Data", m_dataSetIterator.next());
		return (ret);
		}
	}
