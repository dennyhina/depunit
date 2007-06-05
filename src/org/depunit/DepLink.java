package org.depunit;

import java.util.*;

public abstract class DepLink implements Observer
	{
	static public final int HARD_DEPENDENCY = 1;
	static public final int SOFT_DEPENDENCY = 2;
	
	private DepLink m_next;
	private DepLink m_prev;
	private int m_position;
	
	protected class Observers extends Observable
		{
		private Set<DepLink> m_observers;
		private int m_type;
		
		public Observers(int type)
			{
			m_type = type;
			m_observers = new HashSet<DepLink>();
			}
			
		public Set<DepLink> getObservers() { return (m_observers); }
		
		public boolean addObserver(DepLink obs)
			{
			boolean ret = false;
			
			if (m_observers.add(obs))
				{
				ret = true;
				super.addObserver(obs);
				}
				
			return (ret);
			}
			
		public void notifyObservers(String status)
			{
			setChanged();
			notifyObservers(new StateChange(m_type, status));
			clearChanged();
			}
			
		public void reset()
			{
			setChanged();
			notifyObservers(new StateChange(m_type, null));
			clearChanged();
			}
		}
		
	
	protected int m_depCount;
	
	protected Observers m_softObservers;
	protected Observers m_hardObservers;
	
	private boolean m_loopStart;
	
	public DepLink()
		{
		m_loopStart = false;
		m_position = 0;
		m_depCount = 0;
		m_next = null;
		m_prev = null;
		
		m_hardObservers = new Observers(HARD_DEPENDENCY);
		m_softObservers = new Observers(SOFT_DEPENDENCY);
		}
		
	public DepLink getNext() { return (m_next); }
	public void setNext(DepLink next) { m_next = next; }
	
	public DepLink getPrev() { return (m_prev); }
	public void setPrev(DepLink prev) { m_prev = prev; }
	
	public void setPosition(int p) { m_position = p; }
	public int getPosition() { return (m_position); }
	
	public boolean isLoopStart() { return (m_loopStart); }
	public void setLoopStart() { m_loopStart = true; }
	
	public boolean addHardObserver(DepLink obs)
		{
		return (m_hardObservers.addObserver(obs));
		}
		
	public boolean addSoftObserver(DepLink obs)
		{
		return (m_softObservers.addObserver(obs));
		}
		
	public Set<DepLink> getHardObservers()
		{
		return (m_hardObservers.getObservers());
		}
		
	public Set<DepLink> getSoftObservers()
		{
		return (m_softObservers.getObservers());
		}
	}
