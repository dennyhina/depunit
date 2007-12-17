package org.depunit;

import java.util.Map;

public class RunContext
	{
	//Need to preserve a dirty flag for each parameter
	private Map<String, Object> m_params;
	
	public RunContext(Map<String, Object> runParams)
		{
		m_params = runParams;
		}
		
	public void setParam(String name, Object value)
		{
		m_params.put(name.toLowerCase(), value);
		}
		
	public Object getParam(String name)
		{
		return (m_params.get(name.toLowerCase()));
		}
	}
