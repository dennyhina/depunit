package org.depunit;

import java.util.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.*;

public class AntTask extends Task
	{
	private boolean m_regression;
	private String m_reportFile;
	private String m_styleSheet;
	private String m_xmlFile;
	private ArrayList<String> m_testTags;
	private List<String> m_testClasses;
	private List<String> m_testMethods;
	
	public void setRegression(boolean regression) { m_regression = regression; }
	
	public void setReportFile(String reportFile) { m_reportFile = reportFile; }
	
	public void setStyleFile(String styleFile) { m_styleSheet = styleFile; }
	
	public void setTestXmlFile(String xmlFile) { m_xmlFile = xmlFile; }
	
	public void setTestTags(String tags)
		{
		m_testTags = new ArrayList<String>();
		
		String[] split = tags.split(",");
		for (String tag : split)
			m_testTags.add(tag);
		}
	
	@Override
	public void execute() 
			throws BuildException
		{
		Document doc = DepUnit.createResultDocument();
		
		int failCount = DepUnit.runSuite(m_xmlFile, doc, m_testTags);
		
		if (failCount != 0)
			throw new BuildException("DepUnit "+failCount+" test(s) failed");
		}
	}
