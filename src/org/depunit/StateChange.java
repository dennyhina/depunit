package org.depunit;


public class StateChange
	{
	private int m_depType;
	private String m_status;
	
	public StateChange(int depType, String status)
		{
		m_depType = depType;
		m_status = status;
		}
		
	public int getDepType() { return (m_depType); }
	public String getStatus() { return (m_status); }
	
	public String toString()
		{
		return ("StateChange type="+m_depType+" status="+m_status);
		}
	}
