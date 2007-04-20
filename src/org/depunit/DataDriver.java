package org.depunit;

import java.util.Map;

public interface DataDriver
	{
	public void reset();
	public boolean hasNextDataSet();
	public Map<String, ? extends Object> getNextDataSet() throws Exception;
	}
