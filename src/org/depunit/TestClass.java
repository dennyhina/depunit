package org.depunit;

import java.lang.reflect.*;
import java.lang.annotation.*;
import org.depunit.annotations.*;
import java.util.*;

public class TestClass
	{
	public static class InitDataDriver extends DataDriver
		{
		private Map<String, String> m_data;
		private boolean m_reset;
		
		public InitDataDriver(Map<String, String> data)
			{
			m_data = data;
			m_reset = true;
			}
			
		public void reset()
			{
			m_reset = true;
			}
			
		public boolean hasNextDataSet()
			{
			return (m_reset);
			}
			
		public Map<String, ? extends Object>getNextDataSet()
			{
			m_reset = false;
			return (m_data);
			}
		}
		
	private Class m_class;
	private Object m_classInstance;
	private LinkedList<Method> m_beforeTest;
	private LinkedList<Method> m_afterTest;
	//private String m_fullName;
	private LinkedList<TestMethod> m_testMethods;
	//private Map<String, String> m_initParams;
	private DataDriver m_dataDriver;
	
	public TestClass(String className, ClassLoader classLoader)
			throws ClassNotFoundException
		{
		m_dataDriver = null;
		m_classInstance = null;
		//m_initParams = null;
		m_testMethods = new LinkedList<TestMethod>();
		m_beforeTest = new LinkedList<Method>();
		m_afterTest = new LinkedList<Method>();
		List<TestMethod> testMethods = new LinkedList<TestMethod>();
		List<TestMethod> beforeClass = new LinkedList<TestMethod>();
		List<TestMethod> afterClass = new LinkedList<TestMethod>();
		
		m_class = Class.forName(className, true, classLoader);
		
		/* Package p = m_class.getPackage();
		m_fullName = "";
		if (p != null)
			m_fullName = p.getName()+".";
			
		m_fullName = m_class.getName(); */
		
		Method[] methods = m_class.getMethods();
		for (Method m : methods)
			{
			boolean add = false;
			Annotation[] annots = m.getDeclaredAnnotations();
			for (Annotation a : annots)
				{
				if (a instanceof Test)
					testMethods.add(new TestMethod(m, this, false));
					
				else if (a instanceof BeforeTest)
					m_beforeTest.add(m);
					
				else if (a instanceof AfterTest)
					m_afterTest.add(m);
					
				else if (a instanceof BeforeClass)
					beforeClass.add(new TestMethod(m, this, true));
					
				else if (a instanceof AfterClass)
					afterClass.add(new TestMethod(m, this, true));
				}
			}
			
		//setup dependencies
		for (TestMethod tm : testMethods)
			{
			for (TestMethod pretm : beforeClass)
				tm.addHardDependency(pretm.getFullName());
				
			for (TestMethod posttm : afterClass)
				posttm.addSoftDependency(tm.getFullName());
			}
			
		for (TestMethod posttm : afterClass)
			for (TestMethod pretm : beforeClass)
				{
				pretm.addCleanupMethod(posttm.getFullName());
				//posttm.addHardDependency(pretm.getFullName());  //done later on
				}
				
		
		for (TestMethod tm : beforeClass)
			m_testMethods.add(tm);
			
		for (TestMethod tm : testMethods)
			m_testMethods.add(tm);
			
		for (TestMethod tm : afterClass)
			m_testMethods.add(tm);
		}
	
	//---------------------------------------------------------------------------
	public void setDataDriver(DataDriver dd)
		{
		//System.out.println("DataDriver "+dd);
		m_dataDriver = dd;
		}
		
	//---------------------------------------------------------------------------
	public DataDriver getDataDriver()
		{
		return (m_dataDriver);
		}
		
	//---------------------------------------------------------------------------
	public void setInitParams(Map<String, String> initParams)
		{
		m_dataDriver = new InitDataDriver(initParams);
		}
	
	//---------------------------------------------------------------------------
	public String getPackage()
		{
		if (m_class.getPackage() == null)
			return (null);
		else
			return (m_class.getPackage().getName());
		}
	
	//---------------------------------------------------------------------------
	public String getFullName()
		{
		return (m_class.getName());
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets the class with data from the data provider
	*/
	public void initialize()
			throws InitializationException
		{
		Map<String, ? extends Object> dataSet = null;
		try
			{
			dataSet = m_dataDriver.getNextDataSet();
			}
		catch (Exception e)
			{
			throw new InitializationException(e);
			}
			
		BeanUtil.initializeClass(m_class, dataSet, m_classInstance);
		}
		
	//---------------------------------------------------------------------------
	public synchronized Object getClassInstance(Map<String, Object> runContext)
			throws ObjectCreationException
		{
		try
			{
			if (m_classInstance == null)
				{
				try
					{
					Constructor c = m_class.getConstructor(RunContext.class);
					m_classInstance = c.newInstance(new RunContext(runContext));
					}
				catch (Exception e)
					{
					}
					
				if (m_classInstance == null)
					m_classInstance = m_class.newInstance();
				}
				
			//If the context has params that match set methods we will set them
			//We want to do this every time in case the context values change
			Method[] methods = m_class.getMethods();
			for (Method m : methods)
				{
				String methodName = m.getName();
				if (methodName.startsWith("set"))
					{
					String param = methodName.substring(3);
					
					Class<?>[] paramTypes = m.getParameterTypes();
					Object data = runContext.get(param.toLowerCase());
					if ((data != null) && (paramTypes.length == 1) && 
						(data.getClass() == paramTypes[0]))
						{
						m.invoke(m_classInstance, data);							
						}
					}
				}
			}
		catch (InvocationTargetException ite)
			{
			throw new ObjectCreationException(ite);
			}
		catch (InstantiationException ie)
			{
			throw new ObjectCreationException(ie);
			}
		catch (IllegalAccessException iae)
			{
			throw new ObjectCreationException(iae);
			}
			
		return (m_classInstance);
		}
		
	//---------------------------------------------------------------------------
	public void callBeforeTest()
		{
		}
		
	//---------------------------------------------------------------------------
	public void callAfterTest()
		{
		}
		
	//---------------------------------------------------------------------------
	public List<TestMethod> getTestMethods()
		{
		return (m_testMethods);
		}
	}
