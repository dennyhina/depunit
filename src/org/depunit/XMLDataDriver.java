package org.depunit;

import org.w3c.dom.*;
import java.util.*;

public class XMLDataDriver extends DataDriver
	{
	private List<Map<String, String>> m_dataList;
	private int m_listPos;
	
	public XMLDataDriver(Element driver)
		{
		m_listPos = 0;
		m_dataList = new ArrayList<Map<String, String>>();
		
		NodeList nl = driver.getElementsByTagName("data");
		for (int I = 0; I < nl.getLength(); I++)
			{
			Map<String, String> values = new HashMap<String, String>();
			Element data = (Element)nl.item(I);
			NodeList subnl = data.getElementsByTagName("value");
			for (int J = 0; J < subnl.getLength(); J++)
				{
				Element value = (Element)subnl.item(J);
				values.put(value.getAttribute("name"), value.getFirstChild().getNodeValue());
				}
				
			m_dataList.add(values);
			}
		}
		
	public void reset()
		{
		//System.out.println("RESET");
		m_listPos = 0;
		}
		
	public boolean hasNextDataSet()
		{
		//System.out.println("POS: "+m_listPos+" SIZE: "+m_dataList.size());

		boolean ret = m_listPos < m_dataList.size();
		//System.out.println("RET "+ret);

		return (ret);
		}
		
	public Map<String, ? extends Object> getNextDataSet()
		{
		//System.out.println("NEXT");

		Map<String, String> ret = m_dataList.get(m_listPos);
		m_listPos ++;
		
		return (ret);
		}
	}
