package org.depunit;

import org.jargp.*;
import java.lang.reflect.*;
import java.lang.annotation.*;
import org.depunit.annotations.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import static java.lang.System.out;

public class DepUnit
	{
	private static final String REGRESSION_FILE = ".depunit_regression";
	
	private static final ParameterDef[] PARAMETERS = 
		{
		new BoolDef('?', "help"),
		new BoolDef('e', "regression"),
		new StringListDef('c', "classList"),
		new StringDef('r', "reportFile"),
		new StringDef('s', "styleSheet"),
		new StringListDef('t', "tagList"),
		new StringListDef('x', "xmlList"),
		new NoFlagArgDef("targetMethods"),
		new BoolDef('d', "debug")
		};
	
	private static class CommandLine
		{
		public boolean help;
		public boolean debug;
		public boolean regression;
		public List<String> classList;
		public List<String> targetMethods;
		public String reportFile;
		public String styleSheet;
		public List<String> xmlList;
		public ArrayList<String> tagList;
	
		public CommandLine()
			{
			help = false;
			regression = false;
			classList = new ArrayList<String>();
			targetMethods = new ArrayList<String>();
			reportFile = null;
			styleSheet = null;
			xmlList = new ArrayList<String>();
			debug = false;
			tagList = new ArrayList<String>();
			}
		}
		
	
	//---------------------------------------------------------------------------
	/* package */ static Document createResultDocument()
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
	private static void printHelp()
		{
		out.println("DepUnit version X");
		out.println("Usage: java -jar depunit.jar [-e][-v] [-r <report file>] [-s <stylesheet>]");
		out.println("      ([-x <xml file> [-x ...] [-t <tag> [-t ...]]]|([-c <test class> [-c ...]]");
		out.println("      [<target method> ...]))");
		out.println("  -e: Runs DepUnit in regression mode.");
		out.println("  -r: Name of the xml report file to generate.");
		out.println("  -s: Stylesheet to use to style the report.");
		out.println("  -x: XML input file that defines a suite of test runs.");
		out.println("  -c: Test class to include in the run.");
		out.println("  -t: Only test runs marked with this tag will run.");
		out.println("  target methods: Specific test methods to run.");
		}
		
	private static CommandLine cl;
		
	//---------------------------------------------------------------------------
	public static void main(String[] args)
			throws Exception
		{
		int failCount = 0;
		DepUnit du = new DepUnit();
		CommandLine cl = new CommandLine();
		ArgumentProcessor proc = new ArgumentProcessor(PARAMETERS);
		proc.processArgs(args, cl);
		
		if (cl.help || (args.length == 0))
			{
			printHelp();
			return;
			}
			
		du.setDebug(cl.debug);
		du.setRegression(cl.regression);
		du.setReportFile(cl.reportFile);
		du.setStyleSheet(cl.styleSheet);
		du.setTagList(cl.tagList);
		
		Document doc = createResultDocument();
		if (cl.xmlList.size() > 0)
			{
			for (String xmlFile : cl.xmlList)
				{
				failCount += du.runSuite(xmlFile);
				//Have a bailout switch when fail occurs
				}
			}
		else
			{
			failCount = du.runTest(cl.classList, cl.targetMethods);
			}
			
		if (cl.reportFile != null)
			{
			du.writeResults(cl.reportFile, cl.styleSheet);
			}
			
		if (cl.regression)
			{
			du.writeResults(REGRESSION_FILE, null);
			}
			
		if (failCount == 0)
			{
			File regressionFile = new File(REGRESSION_FILE);
			if (regressionFile.exists())
				regressionFile.delete();
			}
			
		if (failCount != 0)
			System.out.println(failCount+" test(s) failed");
			
		System.exit(failCount);
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
	
	private Document m_resultDoc;
	
	private boolean m_help;
	private boolean m_debug;
	private boolean m_regression;
	private String m_reportFile;
	private String m_styleSheet;
	private List<String> m_tagList;
	private ClassLoader m_classLoader;
	
	public DepUnit()
		{
		m_help = false;
		m_regression = false;
		m_reportFile = null;
		m_styleSheet = null;
		m_debug = false;
		m_tagList = new ArrayList<String>();
		
		m_resultDoc = createResultDocument();
		m_classLoader = this.getClass().getClassLoader();
		}
		
	public void setRegression(boolean regres) { m_regression = regres; }
	//public void setClassList(List<String> cl) { m_classList = cl; }
	//public void setTargetMethods(List<String> tm) { m_targetMethods = tm; }
	public void setReportFile(String file) { m_reportFile = file; }
	public void setStyleSheet(String style) { m_styleSheet = style; }
	//public void setXmlList(List<String> xmlList) { m_xmlList = xmlList; }
	public void setTagList(List<String> tagList) { m_tagList = tagList; }
	public void setClassLoader(ClassLoader cl) { m_classLoader = cl; }
	
	public int runSuite(String xmlFile)
			throws ClassNotFoundException, MissingDependencyException, Exception
		{
		int failCount = 0;
		try
			{
			int verbosity = 0;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			//dbf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, false);
			DocumentBuilder db = dbf.newDocumentBuilder();
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
					classList.add(new TestRun.ClassConfig((Element)cnl.item(J), m_classLoader));
					
				classGroups.put(groupName, classList);
				}
				
			//Read in each run
			nl = xmldoc.getElementsByTagName("run");
			for (int I = 0; I < nl.getLength(); I++)
				{
				Element run = (Element)nl.item(I);
				String runName = run.getAttribute("name");
				
				//Look for tags
				if (m_tagList.size() > 0)
					{
					boolean proceed = false;
					NodeList tagNList = run.getElementsByTagName("tag");
					for (int i = 0; i < tagNList.getLength(); i++)
						{
						String tag = tagNList.item(i).getFirstChild().getNodeValue();
						if (m_tagList.contains(tag))
							{
							//We will only run the "run" if it has a tag that was specified
							proceed = true;
							break;
							}
						}
						
					if (!proceed)
						continue;
					}
				
				setVerbosity(verbosity);
				if (verbosity > 0)
					System.out.println("Test Run: "+runName);
				runTest(new TestRun(run, classGroups, m_classLoader));
				
				failCount += getFailedCount();
				}
			}
		catch (InitializationException ie)
			{
			if (ie.getCause() != null)
				{
				System.out.println(ie.getCause());
				ie.getCause().printStackTrace();
				}
			else
				System.out.println(ie);
			}
		/* catch (XMLException xmle)
			{
			System.out.println(xmle);
			}
		catch (Exception e)
			{
			System.out.println(e);
			e.printStackTrace();
			} */
			
		return (failCount);
		}
		
	//---------------------------------------------------------------------------
	public int runTest(List<String> classList, List<String> methodList)
			throws ClassNotFoundException, MissingDependencyException
		{
		return (runTest(new TestRun(classList, methodList)));
		}
		
	//---------------------------------------------------------------------------
	public int runTest(TestRun testRun)
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
		String pattern = testRun.getPattern();
		
		if ((methods.size() == 0) && (pattern == null))
			targetList = getTestMethods("*");
		else
			targetList = new ArrayList<TestMethod>();
			
		if (methods.size() != 0)
			{
			for (TestRun.MethodConfig m : methods)
				{
				targetList.addAll(getTestMethods(m.getName()));
				//Somewhere in here you need to set parameters from the config
				//to the TestMethod class
				}
			}
		
		if (pattern != null)
			targetList.addAll(getTestMethodsFromPattern(pattern));
			
		createProcessQueue(targetList);
		
		//Could call this from a Thread class using multiple threads
		run();
		
		writeReport(testRun.getTestName());
		
		return (getFailedCount());
		}
		
	public void runObjectTests(Object testObject)
		{
		}
		
	public DepUnit(TestRun testRun, boolean debug)
			throws ClassNotFoundException, MissingDependencyException
		{
		
		}
		
	//===========================================================================
	public void setDebug(boolean debug)
		{
		//out.println("Debug = "+debug);
		m_debug = debug;
		}
		
	//---------------------------------------------------------------------------
	public List<TestMethod> getTestMethodsFromPattern(String pattern)
		{
		List<TestMethod> retList = new ArrayList<TestMethod>();
		
		Pattern pat = Pattern.compile(pattern);
		
		for (TestMethod tm : m_testMethods)
			{
			if (pat.matcher(tm.getFullName()).matches())
				retList.add(tm);
			}
			
		return (retList);
		}
		
	//---------------------------------------------------------------------------
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
	private void createProcessQueue(List<TestMethod> targetMethods)
			throws MissingDependencyException
		{
		Set<String> cleanupSet = new HashSet<String>();
		
		if (m_debug)
			{
			out.println("Target methods:");
			Iterator<TestMethod> it = targetMethods.iterator();
			while (it.hasNext())
				{
				TestMethod tm = it.next();
				if (tm != null)
					out.println("  "+tm.getFullName());
				}
			}
			
		//Add methods to target bucket, used for soft dependency lookup
		for (TestMethod tm : targetMethods)
			m_targetBucket.put(tm.getFullName(), tm);
			
		/*
		The next four steps need to be done seperately so recursive dependencies 
		are not introduced
		*/
		
		//Resolving the cleanup methods will add dependencies that must be set before proceeding
		for (TestMethod tm : m_testMethods)  //Resolve all methods
			tm.resolveCleanupMethods(m_tmBucket);
		
		//need to do this first before adding to process queue
		for (TestMethod tm : m_testMethods)  //Resolve all methods
			tm.resolveDependencies(m_groupBucket, m_tmBucket);
			
		//Prep the cleanup methods
		for (TestMethod tm : m_testMethods)
			tm.gatherCleanupDependencies(m_tmBucket);
			
		//If new deps were added in the last set then this will set Observers
		for (TestMethod tm : m_testMethods)
			tm.addCleanupDependencies(m_tmBucket);
			
		//Add only specified target methods if they are not cleanup methods
		for (TestMethod tm : targetMethods)  
			{
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
		System.out.println("Running test");
		TestMethod tm;
		while ((tm = m_processQueue.getNextMethod()) != null)
			{
			TestResult tr = new TestResult(tm);
			m_resultList.add(tr);
			TestClass tc = tm.getTestClass();
			
			//System.out.println("Blocking");
			tm.blockNRun();  //Wait until dependencies are satisfied
			//System.out.println("done Blocking");
			
			try
				{
				if (tm.getStatus() != null)
					{
					tm.skipMethod(m_runParams);
					tr.setStatus(tm.getStatus());
					System.out.println("  Skipping "+tm.getFullName());
					continue;
					}
					
				if (!tm.isProcessMethod())
					tc.callBeforeTest();
					
				//Call tm method
			
				if (m_verbosity > 0)
					System.out.println("  "+tm.getFullName());
				tm.callMethod(m_runParams);
				tr.setStatus(TestResult.STATUS_SUCCESS);
				tm.setStatus(TestMethod.STATUS_SUCCESS);
				}
			catch (ObjectCreationException oce)
				{
				if (m_debug)
					{
					System.out.println("OBJECT CREATION EXCEPTION");
					oce.printStackTrace(System.out);
					}
				//Could not instanciate object
				}
			catch (IllegalAccessException iae)
				{
				if (m_debug)
					{
					System.out.println("ILLEGAL ACCESS EXCEPTION");
					iae.printStackTrace(System.out);
					}
				}
			catch (InitializationException ie)
				{
				if (m_debug)
					System.out.println("INITIALIZATION EXCEPTION");
					
				ie.printStackTrace(System.out);
				tm.setStatus(TestResult.STATUS_FAILED);
				tr.setStatus(TestResult.STATUS_FAILED);
				tr.setException(ie);
				}
			catch (InvocationTargetException ite)
				{
				if (m_debug)
					System.out.println("INVOCATION TARGET EXCEPTION");
					
				Throwable t = ite.getCause();
				tm.setStatus(TestResult.STATUS_FAILED);
				tr.setStatus(TestResult.STATUS_FAILED);
				tr.setException(t);
				while (t != null)
					{
					System.out.println("Cause:");
					System.out.println(tm.printStack(t));
					t = t.getCause();
					}
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
		TestClass tc = new TestClass(className, m_classLoader);
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
			throws MissingDependencyException
		{
		//To make this faster put added methods in a HashSet and check against that
		if (m_processQueue.contains(method))
			return;
		
		if (m_debug)
			out.println("Adding method "+method.getFullName());
			
		//Add hard dependencies
		List<String> hardDep = method.getHardDependencies();
		for (String dep : hardDep)
			{
			TestMethod m = m_tmBucket.get(dep);
			if (m == null)
				throw new MissingDependencyException(method, dep);
			addToProcessQueue(m);
			}
			
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
	public void writeReport(String runName)
		{
		try
			{
			Element root = m_resultDoc.getDocumentElement();
			
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
				
			Element run = m_resultDoc.createElement("run");
			root.appendChild(run);
			run.setAttribute("name", runName);
			run.setAttribute("total", String.valueOf(total));
			run.setAttribute("passed", String.valueOf(passed));
			run.setAttribute("failed", String.valueOf(failed));
			run.setAttribute("skipped", String.valueOf(skipped));
			run.setAttribute("not_ran", String.valueOf(not_ran));
			
			for (TestResult tr : m_resultList)
				{
				Element test = m_resultDoc.createElement("test");
				
				tr.reportStatus(m_resultDoc, test);
				
				run.appendChild(test);
				}
				
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
		
	//---------------------------------------------------------------------------
	public void writeResults(String reportFile, String styleSheet)
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
			trans.transform(new DOMSource(m_resultDoc), new StreamResult(new File(reportFile)));
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
		
	}

