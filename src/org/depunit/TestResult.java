package org.depunit;

import java.util.*;
import org.w3c.dom.*;

public class TestResult
	{
	public static final String STATUS_SUCCESS = "success";
	public static final String STATUS_NOT_RAN = "not_ran";
	public static final String STATUS_SKIPPED = "skipped";
	public static final String STATUS_FAILED = "failed";
	
	private String m_status;
	private TestMethod m_testMethod;
	private Throwable m_exception;  //Stack trace in case of an error condition
	
	public TestResult(TestMethod tm)
		{
		m_testMethod = tm;
		m_status = tm.getStatus();
		}
		
	//---------------------------------------------------------------------------
	public void setStatus(String status)
		{
		m_status = status;
		m_testMethod.setStatus(status);
		}
		
	//---------------------------------------------------------------------------
	public String getStatus() { return (m_status); }
		
	//---------------------------------------------------------------------------
	public void setException(Throwable t)
		{
		m_exception = t;
		}
		
	//---------------------------------------------------------------------------
	public String toString()
		{
		return ("");
		}
		
	//---------------------------------------------------------------------------
	public void reportStatus(Document doc, Element test)
		{
		test.setAttribute("name", "");
		test.setAttribute("class", m_testMethod.getTestClass().getFullName());
		test.setAttribute("method", m_testMethod.getMethodName());
		test.setAttribute("status", m_status);
		
		Element groups = doc.createElement("groups");
		test.appendChild(groups);
		
		//Add to report the parameters that were set
		if (m_status.equals(STATUS_FAILED))
			{
			Element error = doc.createElement("error");
			Element message = doc.createElement("message");
			String msgStr = m_exception.getMessage();
			if (msgStr != null)
				message.appendChild(doc.createTextNode(m_exception.getMessage()));
			error.appendChild(message);
			
			Element stack = doc.createElement("stack");
			error.appendChild(stack);
			
			StackTraceElement[] stackTrace = m_exception.getStackTrace();
			for (StackTraceElement ste : stackTrace)
				{
				if (ste.getMethodName().equals("invoke0"))
					break;
					
				Element trace = doc.createElement("trace");
				trace.setAttribute("className", ste.getClassName());
				trace.setAttribute("fileName", ste.getFileName());
				trace.setAttribute("methodName", ste.getMethodName());
				trace.setAttribute("lineNumber", String.valueOf(ste.getLineNumber()));
				trace.setAttribute("print", ste.toString());
				
				stack.appendChild(trace);
				}
				
			test.appendChild(error);
			}		
		}
	}
