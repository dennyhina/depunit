package org.depunit;


public class MissingDependencyException extends Exception
	{
	
	public MissingDependencyException(String method)
		{
		super("Unable to locate dependency "+method);
		}
	}
