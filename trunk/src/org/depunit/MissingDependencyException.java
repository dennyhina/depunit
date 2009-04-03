package org.depunit;


public class MissingDependencyException extends Exception
	{
	
	public MissingDependencyException(TestMethod test, String method)
		{
		super("Unable to locate dependency "+method+" for test "+test.getFullName());
		}
	}
