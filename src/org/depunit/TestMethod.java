package org.depunit;

import java.lang.reflect.*;
import java.util.*;
import org.depunit.annotations.*;
import org.w3c.dom.*;
import java.io.*;

public class TestMethod extends DepLink
	{
	public static final String STATUS_SUCCESS = "success";
	public static final String STATUS_NOT_RAN = "not_ran";
	public static final String STATUS_SKIPPED = "skipped";
	public static final String STATUS_FAILED = "failed";
	
	/* private abstract class Dependencies implements Observer
		{
		private Set<TestMethod> m_depList;
		protected int m_depCount;
		
		public Dependencies()
			{
			m_depList = new HashSet<TestMethod>();
			}
			
		public void add(TestMethod tm)
			{
			if (m_depList.add(tm))
				{
				tm.addObserver(this);
				m_depCount ++;
				}
			}
			
		protected synchronized void increaseDepCount()
			{
			m_depCount ++;
			}
			
		protected synchronized void reduceDepCount()
			{
			m_depCount --;
			if (m_depCount == 0)
				{
				synchronized(TestMethod.this)
					{
					TestMethod.this.notify();
					}
				}
			}
			
		public int getDepCount()
			{
			return (m_depCount);
			}
		} 
		
	//===========================================================================
	private class SoftObserver extends Dependencies
		{
		public void update(Observable o, Object arg)
			{
			if (arg != null)
				reduceDepCount();
			else
				increaseDepCount();
			}
		}
	
	//===========================================================================
	private class HardObserver extends Dependencies
		{
		public void update(Observable o, Object arg)
			{
			if (arg != null)
				{
				reduceDepCount();
				if (!arg.equals(STATUS_SUCCESS))
					setStatus(STATUS_SKIPPED);
				}
			else
				{
				increaseDepCount();
				clearStatus();
				}
			}
		}*/
		
	//===========================================================================
	private Method m_method;
	private TestClass m_testClass;
	/* private HardObserver m_hardDependencies;
	private SoftObserver m_softDependencies; */
	private String m_status;
	
	private List<String> m_hdMethods;  //Yet to be resolved dependencies
	private List<String> m_sdMethods;  //Yet to be resolved dependencies
	private boolean m_processMethod;   //This method represents a before or after processing method
	private String[] m_groups;         //Groups this method belongs to.
	private List<String> m_cleanupMethods;    //Methods that must be called if this one succeds
	
	
	public TestMethod(Method m, TestClass tc, boolean procMethod)
		{
		//super<TestMethod>();
		m_processMethod = procMethod;
		m_hdMethods = new ArrayList<String>();
		m_sdMethods = new ArrayList<String>();
		m_cleanupMethods = new ArrayList<String>();
		m_status = null;
		/* m_hardDependencies = new HardObserver();
		m_softDependencies = new SoftObserver(); */
		
		m_method = m;
		m_testClass = tc;
		
		Test annot = (Test)m.getAnnotation(Test.class);
		if (annot != null)
			{
			String[] hard = annot.hardDependencyOn();
			for (String h : hard)
				addHardDependency(h);
				
			String[] soft = annot.softDependencyOn();
			for (String s : soft)
				addSoftDependency(s);
				
			m_groups = annot.groups();
			String cleanupMethod = annot.cleanupMethod();
			if (!cleanupMethod.equals(""))
				m_cleanupMethods.add(cleanupMethod);
			}
		}
		
	//---------------------------------------------------------------------------
	public synchronized void update(Observable o, Object arg)
		{
		StateChange sc = (StateChange)arg;
		
		String status = sc.getStatus();
		if (status != null)
			{
			if ((sc.getDepType() == HARD_DEPENDENCY) && (!status.equals(STATUS_SUCCESS)))
				setStatus(STATUS_SKIPPED);
					
			m_depCount --;
			if (m_depCount == 0)
				notify();
			}
		else
			{
			m_depCount ++;
			reset();
			}
		}
		
	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------
	public void resolveDependencies(Map<String, List<TestMethod> > groupBucket,
				Map<String, TestMethod> methods)
			throws MissingDependencyException
		{
		ListIterator<String> it = m_cleanupMethods.listIterator();
		while (it.hasNext())
			{
			String method = it.next();
			
			if (methods.get(m_testClass.getFullName()+"."+method) != null)
				{
				method = m_testClass.getFullName()+"."+method;
				it.set(method);
				}
			else if (methods.get(method) == null)
				throw new MissingDependencyException(method);
				
			TestMethod tm = methods.get(method);
			tm.addHardDependency(getFullName());
			}
			
		it = m_hdMethods.listIterator();
		while (it.hasNext())
			{
			String method = it.next();
			
			//Check to see if it is a group first
			List<TestMethod> group = groupBucket.get(method);
			if (group != null)
				{
				it.remove(); //Remove group name
				for (TestMethod tm : group)
					{
					it.add(tm.getFullName());  //Add members of group
					//m_hardDependencies.add(tm);
					if (tm.addHardObserver(this))
						m_depCount ++;
					}
				}
			else
				{ //Check to see if name is only partial
				TestMethod tm = methods.get(m_testClass.getFullName()+"."+method);
				if (tm != null)
					{
					it.set(m_testClass.getFullName()+"."+method);
					//m_hardDependencies.add(tm);
					if (tm.addHardObserver(this))
						m_depCount ++;
					}
				else if ((tm = methods.get(method)) != null)
					//m_hardDependencies.add(tm);
					if (tm.addHardObserver(this))
						m_depCount ++;
				else
					throw new MissingDependencyException(method);
				}
			}
			
		
		it = m_sdMethods.listIterator();
		while (it.hasNext())
			{
			String method = it.next();
			
			//Check to se if it is a group first
			List<TestMethod> group = groupBucket.get(method);
			if (group != null)
				{
				it.remove(); //Remove group name
				for (TestMethod tm : group)
					{
					it.add(tm.getFullName());  //Add members of group
					//m_softDependencies.add(tm);
					if (tm.addSoftObserver(this))
						m_depCount ++;
					}
				}
			else
				{
				TestMethod tm = methods.get(m_testClass.getFullName()+"."+method);
				if (tm != null)
					{
					it.set(m_testClass.getFullName()+"."+method);
					//m_softDependencies.add(tm);
					if (tm.addSoftObserver(this))
						m_depCount ++;
					}
				else if ((tm = methods.get(method)) != null)
					//m_softDependencies.add(tm);
					if (tm.addSoftObserver(this))
						m_depCount ++;
				else
					throw new MissingDependencyException(method);
				}
			}
		}
		
	//---------------------------------------------------------------------------
	public boolean isProcessMethod()
		{
		return (m_processMethod);
		}
	
	//---------------------------------------------------------------------------
	public List<String> getHardDependencies()
		{
		return (m_hdMethods);
		}
		
	//---------------------------------------------------------------------------
	public List<String> getSoftDependencies()
		{
		return (m_sdMethods);
		}
		
	//---------------------------------------------------------------------------
	public String[] getGroups()
		{
		return (m_groups);
		}
		
	//---------------------------------------------------------------------------
	public List<String> getCleanupMethods()
		{
		return (m_cleanupMethods);
		}
		
	//---------------------------------------------------------------------------
	public void addCleanupMethod(String method)
		{
		m_cleanupMethods.add(method);
		}
		
	//---------------------------------------------------------------------------
	public String getFullName()
		{
		return (m_testClass.getFullName()+"."+m_method.getName());
		}
		
	//---------------------------------------------------------------------------
	public TestClass getTestClass()
		{
		return (m_testClass);
		}
		
	//---------------------------------------------------------------------------
	public String getMethodName()
		{
		return (m_method.getName());
		}
		
	//---------------------------------------------------------------------------
	public void addSoftDependency(String method)
		{
		m_sdMethods.add(method);
		}
		
	//---------------------------------------------------------------------------
	public void addHardDependency(String method)
		{
		m_hdMethods.add(method);
		}
		
	//---------------------------------------------------------------------------
	//Probably add more to this
	public synchronized void setStatus(String status)
		{
		if (m_status == null)
			{
			m_status = status;
			m_hardObservers.notifyObservers(status);
			m_softObservers.notifyObservers(status);
			}
		}
		
	//---------------------------------------------------------------------------
	private synchronized void reset()
		{
		resetStatus();
		
		//Check for data driver and reset it
		DataDriver dd = getTestClass().getDataDriver();
		if (dd != null)
			dd.reset();
		}
		
	//---------------------------------------------------------------------------
	public synchronized void resetStatus()
		{
		if (m_status != null)
			{
			m_status = null;
			m_hardObservers.reset();
			}
		}
		
	//---------------------------------------------------------------------------
	public String getStatus()
		{
		return (m_status);
		}

	//---------------------------------------------------------------------------
	public int hashCode()
		{
		return (getFullName().hashCode());
		}
		
	//---------------------------------------------------------------------------
	public synchronized void blockNRun()
		{
		//System.out.println("Status = "+m_status);
		//System.out.println("m_depCount = "+m_depCount);
		//System.out.println("Method = "+getFullName());
		while ((m_depCount != 0) &&
				(m_status == null))
			{
			try
				{
				wait();
				}
			catch (InterruptedException ie)
				{
				}
			}
		}
		
	//---------------------------------------------------------------------------
	public void callMethod(Map<String, Object> runParams)
			throws ObjectCreationException, IllegalAccessException,
					InvocationTargetException, InitializationException
		{
		Object obj = m_testClass.getClassInstance(runParams);
		
		//Test for begining of a data loop, this means he is the first method of a class with a data provider
		if (isLoopStart())
			m_testClass.initialize();
			
		m_method.invoke(obj);
		}
		
	//---------------------------------------------------------------------------
	public void reportStatus(Document doc, Element test)
		{
		test.setAttribute("name", "");
		test.setAttribute("class", m_testClass.getFullName());
		test.setAttribute("method", m_method.getName());
		test.setAttribute("status", m_status);
		
		Element groups = doc.createElement("groups");
		test.appendChild(groups);
		
		if (m_groups != null)
			for (String groupName : m_groups)
				{
				Element group = doc.createElement("group");
				group.appendChild(doc.createTextNode(groupName));
				groups.appendChild(group);
				}
				
		}
	
	//---------------------------------------------------------------------------
	public String toString()
		{
		StringBuilder sb = new StringBuilder();
		
		return (sb.toString());
		}
		
	//---------------------------------------------------------------------------
	public String printStack(Throwable t)
		{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		boolean inMethod = false;
		
		String method = m_method.getName();
		String klass = m_testClass.getFullName();
		StackTraceElement[] stack = t.getStackTrace();
		pw.println(t.getClass().getName()+": "+t.getMessage());
		
		for (StackTraceElement e : stack)
			{
			if (method.equals(e.getMethodName()) && klass.equals(e.getClassName()))
				inMethod = true;
			else if (inMethod)
				break;
				
			pw.println("   at "+e.getClassName()+"."+e.getMethodName()+"("+
					e.getFileName()+":"+e.getLineNumber()+")");
			}
		
		pw.close();
		return (sw.toString());
		}
	}
