package org.depunit;


import javax.xml.xpath.*;
import org.w3c.dom.*;
import java.util.*;

/**
	This class represents all the data for one test run
*/
public class TestRun
	{
	public static class ClassConfig
		{
		private String m_name;
		private Map<String, String> m_params;
		private DataDriver m_dataDriver;
		
		public ClassConfig(String name)
			{
			m_name = name;
			m_params = new HashMap<String, String>();
			m_dataDriver = null;
			}
			
		public ClassConfig(Element e)
				throws XMLException
			{
			m_params = null;
			m_name = e.getAttribute("name");
			if (m_name == null)
				throw new XMLException("Missing name attribute on class definition");
			
			NodeList nl = e.getElementsByTagName("driver");
			if (nl.getLength() != 0)
				{
				m_dataDriver = new XMLDataDriver((Element)nl.item(0));
				}
			else
				{
				nl = e.getElementsByTagName("value");
				if (nl.getLength() != 0)
					m_params = new HashMap<String, String>();
					
				for (int I = 0; I < nl.getLength(); I++)
					{
					Element val = (Element)nl.item(I);
					m_params.put(val.getAttribute("name"), val.getFirstChild().getNodeValue());
					}
				}
			}
			
		public String getName() { return (m_name); }
		public Map<String, String> getParams() { return (m_params); }
		public DataDriver getDataDriver() { return (m_dataDriver); }
		}
		
		
	public static class MethodConfig
		{
		private String m_name;
		
		public MethodConfig(String name)
			{
			m_name = name;
			}
			
		public MethodConfig(Element e)
			{
			}
			
		public String getName() { return (m_name); }
		}
		
	
		
		
	private Map<String, ClassConfig> m_classes;
	private List<MethodConfig> m_methods;
	private XPath m_xpath;
	
	public TestRun(List<String> classes, List<String> methods)
		{
		m_classes = new HashMap<String, ClassConfig>();
		m_methods = new ArrayList<MethodConfig>();
		
		for (String cl : classes)
			m_classes.put(cl, new ClassConfig(cl));
			
		for (String m : methods)
			m_methods.add(new MethodConfig(m));
		}
		
	private Element getElement(Element parent, String name)
		{
		NodeList nl = parent.getElementsByTagName(name);
		if (nl.getLength() == 0)
			return (null);
		else
			return ((Element)nl.item(0));
		}
		
	public TestRun(Element e, Map<String, List<ClassConfig>> classGroups)
			throws XMLException
		{
		m_classes = new HashMap<String, ClassConfig>();
		m_methods = new ArrayList<MethodConfig>();
		
		Element classes = getElement(e, "classes");
		if (classes == null)
			throw new XMLException("Missing classes tag");
			
		NodeList nl = classes.getElementsByTagName("includeGroup");
		for (int I = 0; I < nl.getLength(); I++)
			{
			String groupName = nl.item(I).getFirstChild().getNodeValue();
			List<ClassConfig> groupList = classGroups.get(groupName);
			for (ClassConfig cc : groupList)
				m_classes.put(cc.getName(), cc);
			}
			
		nl = classes.getElementsByTagName("class");
		for (int I = 0; I < nl.getLength(); I++)
			{
			ClassConfig cc = new ClassConfig((Element)nl.item(I));
			m_classes.put(cc.getName(), cc);
			}
			
		Element tests = getElement(e, "tests");
		if (tests != null)
			{
			nl = tests.getElementsByTagName("method");
			for (int I = 0; I < nl.getLength(); I++)
				m_methods.add(new MethodConfig(nl.item(I).getFirstChild().getNodeValue()));
			}
		}
		
	public Collection<ClassConfig> getClasses()
		{
		return (m_classes.values());
		}
		
	public List<MethodConfig> getMethods()
		{
		return (m_methods);
		}
		
	}
