package org.depunit;

import java.util.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.BuildException;
import org.w3c.dom.*;

public class AntTask extends Task
	{
	private boolean m_regression;
	private boolean m_debug;
	private String m_reportFile;
	private String m_styleSheet;
	private String m_xmlFile;
	private ArrayList<String> m_testTags;
	private List<String> m_testClasses;
	private List<String> m_testMethods;
	
	private Path m_classPath;
	
	public void setRegression(boolean regression) { m_regression = regression; }
	
	public void setDebug(boolean debug) { m_debug = debug; }
	
	public void setReportFile(Path reportFile) { m_reportFile = reportFile.toString(); }
	
	public void setStyleFile(Path styleFile) { m_styleSheet = styleFile.toString(); }
	
	public void setTestXmlFile(Path xmlFile) { m_xmlFile = xmlFile.toString(); }
	
	public void setTestTags(String tags)
		{
		m_testTags = new ArrayList<String>();
		
		if ((tags != null) && (tags.length() != 0))
			{
			String[] split = tags.split(",");
			for (String tag : split)
				m_testTags.add(tag);
			}
		}
		
	/* public void addClasspath(String resource)
		{
		System.out.println("addClasspath");
		System.out.println(resource);
		}
		
	public void setClasspath(Path path)
		{
		System.out.println("setClasspath");
		System.out.println(path);
		}
		
	public void setClasspathRef(Reference r)
		{
		System.out.println("setClasspathRef");
		System.out.println(r);
		}
		
	public void add(Path p)
		{
		System.out.println("add");
		System.out.println(p);
		} */
		
	public Path createClasspath() 
		{
		System.out.println("createClasspath");
		if (m_classPath == null)
			m_classPath = new Path(getProject());
			
		return (m_classPath.createPath());
		}
	
	@Override
	public void execute() 
			throws BuildException
		{
		//System.out.println(m_classPath);
		try
			{
			DepUnit du = new DepUnit();
			du.setDebug(m_debug);
			du.setTagList(m_testTags);
			if (m_classPath != null)
				du.setClassLoader(new AntClassLoader(getProject(), m_classPath));
			
			int failCount = du.runSuite(m_xmlFile);
			
			if (m_reportFile != null)
				du.writeResults(m_reportFile, m_styleSheet);
			
			if (failCount != 0)
				throw new BuildException("DepUnit "+failCount+" test(s) failed");
			}
		catch (Exception e)
			{
			throw new BuildException(e);
			}
		}
	}
