package org.depunit;

import java.util.*;

public class ProcessQueue
	{
	private class ProcessLoop
		{
		private DepLink m_start;
		private DepLink m_end;
		private ProcessLoop m_innerLoop;
		private DepLink m_loopPos;
		private DataDriver m_dataDriver;
		
		public ProcessLoop(DepLink start, DataDriver dd)
			{
			//System.out.println("Innerloop");
			start.setLoopStart();
			m_dataDriver = dd;
			m_innerLoop = null;
			m_start = start;
			m_loopPos = start;
			// TODO: find end
			m_end = getLastLink(m_start);
			}
			
		private DepLink getLastLink(DepLink start)
			{
			DepLink last = start;
			Set<DepLink> set = start.getHardObservers();

			Iterator<DepLink> it = set.iterator();
			while (it.hasNext())
				{
				//We recursively look for the very last dependency link
				DepLink dl = getLastLink(it.next());
				if (dl.getPosition() > last.getPosition())
					last = dl;
				}
				
			return (last);
			}
			
		public DepLink getLoopEnd() { return (m_end); }
			
		public DepLink getNextMethod()
			{
			DepLink ret = null;
			if (m_innerLoop != null)
				{
				ret = m_innerLoop.getNextMethod();
				if (ret == null)  //Inner loop is done
					{
					//System.out.println("Inner loop done");
					m_loopPos = m_innerLoop.getLoopEnd().getNext();
					m_innerLoop = null;
					}
				}
				
			if ((ret == null) && (m_loopPos != m_end.getNext()))
				{
				ret = m_loopPos;
				m_loopPos = m_loopPos.getNext();
				//Check for Data driver
				DataDriver dd = ((TestMethod)ret).getTestClass().getDataDriver();
				if ((dd != null) && (dd != m_dataDriver) && dd.hasNextDataSet())
					{
					//System.out.println("NEW PROCESS LOOP");

					m_innerLoop = new ProcessLoop(ret, dd);
					ret = m_innerLoop.getNextMethod();
					}
				}
				
			//If still null check to see if we need to loop again
			if (ret == null)
				{
				if (m_dataDriver.hasNextDataSet())
					{
					m_loopPos = m_start.getNext();
					ret = m_start;
					m_dataDriver.lockReset();
					((TestMethod)ret).resetStatus();
					m_dataDriver.unlockReset();
					}
				else
					m_dataDriver.reset();
				}
				
			return (ret);
			}
		}
	
	
	private HashSet<TestMethod> m_queueLookup;
	//private List<TestMethod> m_processList;
	//private int m_runPos;
	private ProcessLoop m_innerLoop;
	
	private DepLink m_head;
	private DepLink m_tail;
	private DepLink m_runPos;
	
	public ProcessQueue()
		{
		m_head = new DepLink() //Create a dead head
				{
				public void update(Observable o, Object arg) {}
				};
		m_tail = m_head;
		m_runPos = m_head;
		m_queueLookup = new HashSet<TestMethod>();
		//m_processList = new ArrayList<TestMethod>();
		}
		
	public boolean contains(TestMethod tm)
		{
		return (m_queueLookup.contains(tm));
		}
		
	public void add(TestMethod tm)
		{
		//System.out.println("Adding "+tm.getFullName());
		m_queueLookup.add(tm);
		//System.out.println("Position "+m_queueLookup.size());
		tm.setPosition(m_queueLookup.size());
		//m_processList.add(tm);
		m_tail.setNext(tm);
		m_tail = tm;
		}
		
	public synchronized TestMethod getNextMethod()
		{
		//System.out.println("ProcessQueue.getNextMethod");
		TestMethod ret = null;
		if (m_innerLoop != null)
			{
			ret = (TestMethod)m_innerLoop.getNextMethod();
			if (ret == null) //Inner loop is done
				{
				//System.out.println("Loop done");
				m_runPos = m_innerLoop.getLoopEnd();
				m_innerLoop = null;
				}
			}
		
		if ((ret == null) && ((ret = (TestMethod)m_runPos.getNext()) != null))
			{
			//Check ret for data driver
			DataDriver dd = ret.getTestClass().getDataDriver();
			if (dd != null)
				{
				//System.out.println("NEW PROCES LOOP");

				m_innerLoop = new ProcessLoop(ret, dd);
				ret = (TestMethod)m_innerLoop.getNextMethod();
				}
				
			m_runPos = m_runPos.getNext();
			}
			
		/* if (ret != null)
			System.out.println("Returning "+ret.getFullName()); */
			
		return (ret);
		}
	}
