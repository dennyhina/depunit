package org.depunit;

import org.jargp.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import org.depunit.annotations.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

public class DepUnit
	{
	private static final ParameterDef[] PARAMETERS = 
		{
		new BoolDef('?', "help"),
		new StringListDef('c', "classList"),
		new StringDef('r', "reportFile"),
		new StringDef('s', "styleSheet"),
		new StringListDef('x', "xmlList"),
		new NoFlagArgDef("targetMethods")
		};
	
	private static class CommandLine
		{
		public boolean help;
		public List<String> classList;
		public List<String> targetMethods;
		public String reportFile;
		public String styleSheet;
		public List<String> xmlList;
		
		public CommandLine()
			{
			help = false;
			classList = new ArrayList<String>();
			targetMethods = new ArrayList<String>();
			reportFile = null;
			styleSheet = null;
			xmlList = new ArrayList<String>();
			}
		}
		
		
	//---------------------------------------------------------------------------
	private static void writeResults(Document doc, String reportFile, String styleSheet)
		{
		try
			{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans;
			if (styleSheet != null)
				trans = tf.newTransformer(new StreamSource(new File(styleSheet)));
			else
				trans = tf.newTransformer();
				
			trans.setOutputProperty("indent", "yes");
			trans.transform(new DOMSource(doc), new StreamResult(new File(reportFile)));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
	
	//---------------------------------------------------------------------------
	private static Document createResultDocument()
		{
		Document doc = null;
		
		try
			{
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			DOMImplementation di = db.getDOMImplementation();			
			doc = di.createDocument(null, "test_results", null);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		
		return (doc);
		}
		
	//---------------------------------------------------------------------------
	public static void main(String[] args)
			throws Exception
		{
		int failCount = 0;
		CommandLine cl = new CommandLine();
		ArgumentProcessor proc = new ArgumentProcessor(PARAMETERS);
		proc.processArgs(args, cl);
		
		Document doc = createResultDocument();
		if (cl.xmlList.size() > 0)
			{
			for (String xmlFile : cl.xmlList)
				{
				failCount += runSuite(xmlFile, doc);
				//Have a bailout switch when fail occurs
				}
			}
		else
			{
			DepUnit du = new DepUnit(new TestRun(cl.classList, cl.targetMethods));
		
			//Could call this from a Thread class using multiple threads
			du.run();
		
			du.writeReport(doc);
			
			failCount = du.getFailedCount();
			}
			
		if (cl.reportFile != null)
			{
			writeResults(doc, cl.reportFile, cl.styleSheet);
			}
			
		System.exit(failCount);
		}
		
	//---------------------------------------------------------------------------
	private static int runSuite(String xmlFile, Document results)
		{
		int failCount = 0;
		try
			{
			int verbosity = 0;
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmldoc = db.parse(new File(xmlFile));
			
			Element root = xmldoc.getDocumentElement();
			String verbose = root.getAttribute("verbose");
			if ((verbose != null) && (!verbose.equals("")))
				verbosity = Integer.parseInt(verbose);
			
			//Read in the class groups
			NodeList nl = xmldoc.getElementsByTagName("classGroup");
			
			Map<String, List<TestRun.ClassConfig>> classGroups = new HashMap<String, 
					List<TestRun.ClassConfig>>();
					
			for (int I = 0; I < nl.getLength(); I++)
				{
				Element group = (Element)nl.item(I);
				String groupName = group.getAttribute("name");
				
				NodeList cnl = group.getElementsByTagName("class");
				List<TestRun.ClassConfig> classList = new ArrayList<TestRun.ClassConfig>();
				
				for (int J = 0; J < cnl.getLength(); J++)
					classList.add(new TestRun.ClassConfig((Element)cnl.item(J)));
					
				classGroups.put(groupName, classList);
				}
				
			//Read in each run
			nl = xmldoc.getElementsByTagName("run");
			for (int I = 0; I < nl.getLength(); I++)
				{
				Element run = (Element)nl.item(I);
				String runName = run.getAttribute("name");
				
				DepUnit du = new DepUnit(new TestRun(run, classGroups));
				du.setVerbosity(verbosity);
				
				if (verbosity > 0)
					System.out.println("Test Run: "+runName);
				du.run();
				du.writeReport(results);
				failCount += du.getFailedCount();
				}
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
			
		return (failCount);
		}
	
	//===========================================================================
	//private HashSet<TestMethod> m_queueLookup;
	//private Queue<TestMethod> m_processQueue;
	//private List<TestMethod> m_reportList;  //Contains the same as m_processQueue, used for reporting
	private ProcessQueue m_processQueue;
	private Queue<TestResult> m_resultList;
	private HashMap<String, TestMethod> m_tmBucket;
	private HashMap<String, TestMethod> m_targetBucket;
	private HashMap<String, List<TestMethod> > m_groupBucket;
	private List<TestMethod> m_testMethods;
	private Stack<TestMethod> m_cleanupStack;
	private Map<String, Object> m_runParams;
	private int m_verbosity;
	
	public DepUnit(TestRun testRun)
			throws ClassNotFoundException, MissingDependencyException
		{
		//System.out.println("New DepUnit");
		m_verbosity = 0;
		//m_queueLookup = new HashSet<TestMethod>();
		//m_processQueue = new ConcurrentLinkedQueue<TestMethod>();
		//m_reportList = new ArrayList<TestMethod>();
		m_processQueue = new ProcessQueue();
		m_resultList = new ConcurrentLinkedQueue<TestResult>();
		m_tmBucket = new HashMap<String, TestMethod>();
		m_targetBucket = new HashMap<String, TestMethod>();
		m_testMethods = new LinkedList<TestMethod>();
		m_cleanupStack = new Stack<TestMethod>();
		m_groupBucket = new HashMap<String, List<TestMethod> >();
		m_runParams = new HashMap<String, Object>();
		
		Collection<TestRun.ClassConfig> classes = testRun.getClasses();
		for (TestRun.ClassConfig cc : classes)
			{
			if (cc.getDataDriver() != null)
				addClass(cc.getName(), cc.getDataDriver());
			else
				addClass(cc.getName(), cc.getParams());
			}
		
		List<TestMethod> targetList;
		List<TestRun.MethodConfig> methods = testRun.getMethods();
		if (methods.size() == 0)
			targetList = getTestMethods("*");
		else
			{
			targetList = new ArrayList<TestMethod>();
			for (TestRun.MethodConfig m : methods)
				{
				targetList.addAll(getTestMethods(m.getName()));
				//Somewhere in here you need to set parameters from the config
				//to the TestMethod class
				}
			}
			
		createProcessQueue(targetList);
		}
		
	//===========================================================================
	public List<TestMethod> getTestMethods(String name)
		{
		List<TestMethod> retList;
		
		if (name.equals("*"))
			retList = new ArrayList<TestMethod>(m_testMethods);
		else if ((retList = m_groupBucket.get(name) )!= null)
			retList = new ArrayList<TestMethod>(retList);
		else
			{
			retList = new ArrayList<TestMethod>();
			retList.add(m_tmBucket.get(name));
			}
			
		return (retList);
		}
		
	//---------------------------------------------------------------------------
	public void setVerbosity(int level)
		{
		m_verbosity = level;
		}
		
	//---------------------------------------------------------------------------
	public void createProcessQueue(List<TestMethod> targetMethods)
			throws MissingDependencyException
		{
		Set<String> cleanupSet = new HashSet<String>();
		
		//Add methods to target bucket, used for soft dependency lookup
		for (TestMethod tm : targetMethods)
			m_targetBucket.put(tm.getFullName(), tm);
			
		//need to do this first before adding to process queue
		for (TestMethod tm : m_testMethods)  //Resolve all methods
			tm.resolveDependencies(m_groupBucket, m_tmBucket);
			
		//Create a set of cleanup methods
		for (TestMethod tm : m_testMethods)
			cleanupSet.addAll(tm.getCleanupMethods());
			
		//Add only specified target methods if they are not cleanup methods
		for (TestMethod tm : targetMethods)  
			{
			if (!cleanupSet.contains(tm.getFullName()))
				addToProcessQueue(tm);
			}
			
		//Adds cleanup methods to the end of the run
		while (!m_cleanupStack.empty())
			addToProcessQueue(m_cleanupStack.pop());
			
		//Now mark all other methods as not_run
		for (TestMethod tm : m_testMethods)
			{
			if (!m_processQueue.contains(tm))
				tm.setStatus(TestMethod.STATUS_NOT_RAN);
			}
		}
		
	//---------------------------------------------------------------------------
	public void run()
		{
		TestMethod tm;
		while ((tm = m_processQueue.getNextMethod()) != null)
			{
			TestResult tr = new TestResult(tm);
			m_resultList.add(tr);
			TestClass tc = tm.getTestClass();
			
			//System.out.println("Blocking");
			tm.blockNRun();  //Wait until dependencies are satisfied
			//System.out.println("done Blocking");
			
			if (tm.getStatus() != null)
				{
				tr.setStatus(tm.getStatus());
				System.out.println("Skipping "+tm.getFullName());
				continue;
				}
				
			if (!tm.isProcessMethod())
				tc.callBeforeTest();
				
			//Call tm method
			try
				{
				if (m_verbosity > 0)
					System.out.println("  "+tm.getFullName());
				tm.callMethod(m_runParams);
				tr.setStatus(TestResult.STATUS_SUCCESS);
				tm.setStatus(TestMethod.STATUS_SUCCESS);
				}
			catch (ObjectCreationException oce)
				{
				//Could not instanciate object
				}
			catch (IllegalAccessException iae)
				{
				}
			catch (InitializationException ie)
				{
				ie.printStackTrace(System.out);
				tr.setStatus(TestResult.STATUS_FAILED);
				tr.setException(ie);
				}
			catch (InvocationTargetException ite)
				{
				Throwable t = ite.getCause();
				tr.setStatus(TestResult.STATUS_FAILED);
				tr.setException(t);
				System.out.println(tm.printStack(t));
				//t.printStackTrace(System.out);
				}
			
			
			if (!tm.isProcessMethod())
				tc.callAfterTest();
			}
		}
		
	//---------------------------------------------------------------------------
	private void addToGroupBucket(TestMethod tm)
		{
		String[] groups = tm.getGroups();
		if (groups == null)
			return;
			
		for (String group : groups)
			{
			List groupList = m_groupBucket.get(group);
			if (groupList == null)
				{
				groupList = new ArrayList<TestMethod>();
				m_groupBucket.put(group, groupList);
				}
				
			groupList.add(tm);
			}
		}
		
	//---------------------------------------------------------------------------
	private void addClass(String className, DataDriver dd)
			throws ClassNotFoundException
		{
		TestClass tc = new TestClass(className);
		tc.setDataDriver(dd);
		List<TestMethod> methods = tc.getTestMethods();
		
		for (TestMethod tm : methods)
			{
			m_tmBucket.put(tm.getFullName(), tm);
			m_testMethods.add(tm);
			addToGroupBucket(tm);
			}
		}
		
	//---------------------------------------------------------------------------
	public void addClass(String className, Map<String, String> initParams)
			throws ClassNotFoundException
		{
		if (initParams != null)
			addClass(className, new TestClass.InitDataDriver(initParams));
		else
			addClass(className, (DataDriver)null);
		}
		
	//---------------------------------------------------------------------------
	public void addToProcessQueue(TestMethod method)
		{
		//To make this faster put added methods in a HashSet and check against that
		if (m_processQueue.contains(method))
			return;
		
		//Add hard dependencies
		List<String> hardDep = method.getHardDependencies();
		for (String dep : hardDep)
			addToProcessQueue(m_tmBucket.get(dep));
			
		//Add soft dependencies
		List<String> softDep = method.getSoftDependencies();
		for (String dep : softDep)
			{
			TestMethod tm = m_targetBucket.get(dep);
			if (tm != null)
				addToProcessQueue(tm);
			}
		
		//add to queue
		m_processQueue.add(method);
		
		//Add cleanup methods to cleanup list
		List<String> cleanupList = method.getCleanupMethods();
		for (String cleanup : cleanupList)
			m_cleanupStack.push(m_tmBucket.get(cleanup));
		}
		
	//---------------------------------------------------------------------------
	public int getFailedCount()
		{
		int count = 0;
		
		for (TestResult tr : m_resultList)
			{
			if (tr.getStatus().equals(TestResult.STATUS_FAILED))
				count ++;
			}
			
		return (count);
		}
		
	//---------------------------------------------------------------------------
	//public void writeReport(String fileName, String styleSheet)
	public void writeReport(Document doc)
		{
		try
			{
			Element root = doc.getDocumentElement();
			
			int total = m_testMethods.size();
			int passed = 0;
			int failed = 0;
			int skipped = 0;
			int not_ran = 0;
			
			for (TestResult tr : m_resultList)
				{
				String status = tr.getStatus();
				if (status.equals(TestMethod.STATUS_SUCCESS))
					passed ++;
				else if (status.equals(TestMethod.STATUS_FAILED))
					failed ++;
				else if (status.equals(TestMethod.STATUS_SKIPPED))
					skipped ++;
				else
					not_ran ++;
				}
				
			Element run = doc.createElement("run");
			root.appendChild(run);
			run.setAttribute("total", String.valueOf(total));
			run.setAttribute("passed", String.valueOf(passed));
			run.setAttribute("failed", String.valueOf(failed));
			run.setAttribute("skipped", String.valueOf(skipped));
			run.setAttribute("not_ran", String.valueOf(not_ran));
			
			for (TestResult tr : m_resultList)
				{
				Element test = doc.createElement("test");
				
				tr.reportStatus(doc, test);
				
				run.appendChild(test);
				}
				
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
			
		}
	}

