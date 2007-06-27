package org.depunit;

import java.lang.reflect.*;
import java.util.*;


public class BeanUtil
	{
	public static void initializeClass(Class klass, Map<String, ? extends Object> dataSet,
			Object instance)
			throws InitializationException
		{
		try
			{
			Method[] methods = klass.getMethods();
			Map<String, Method> methodMap = new HashMap();
			
			for (Method m : methods)
				methodMap.put(m.getName().toLowerCase(), m);
				
			Set<String> paramNames = dataSet.keySet();
			for (String param : paramNames)
				{
				Method m = methodMap.get("set"+param.toLowerCase());
				if (m == null)
					throw new InitializationException("Unable to locate method on "+klass.getName()+" to set param "+param);
				//System.out.println("Calling "+m.getName()+" with param "+dataSet.get(param)+" on class "+m_classInstance);
				
				if (dataSet.get(param) instanceof String)
					{
					Class type = m.getParameterTypes()[0]; //This assumes a lot
					if (type == String.class)
						m.invoke(instance, dataSet.get(param));
					else if (type == int.class)
						m.invoke(instance, new Integer((String)dataSet.get(param)));
					else
						System.out.println("Param type = "+type);
					}
				else
					m.invoke(instance, dataSet.get(param));
				}
			}
		catch (IllegalAccessException iae)
			{
			throw new InitializationException(iae);
			}
		catch (InvocationTargetException ite)
			{
			throw new InitializationException(ite);
			}
		}
	}
