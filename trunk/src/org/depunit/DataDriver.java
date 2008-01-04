package org.depunit;

import java.util.Map;

/**
Data drivers work like Java Iterators.  It lets DepUnit iterate over
a set of test data.
*/
public abstract class DataDriver
	{
	private boolean m_locked = false;
	
	void internalReset()
		{
		if (!m_locked)
			reset();
		}
		
	void lockReset() { m_locked = true; }
	void unlockReset() { m_locked = false; }
		
	/**
	If this data driver is nested within another data driver the reset is called
	for every iteration of the outer data driver
	*/
	public abstract void reset();
	
	/**
	Returns true if there is more data to be retrieved from getNextDataSet
	*/
	public abstract boolean hasNextDataSet();
	
	/**
	Returns a map of values to be set on the TestBean object before running tests.
	If the test class has a method
	<code>
	setUserName();
	setPassword();
	</code> 
	then the map will contain something like
	<code>
	{
		{ "UserName", "Bob" },
		{ "Password", "secret" }
	}
	</code>
	*/
	public abstract Map<String, ? extends Object> getNextDataSet() throws Exception;
	}
