package com.naskar.pls.service.impl;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.naskar.pls.service.DataSourceService;

public class DataSourceServiceImpl implements DataSourceService {
	
	@Override
	public String definePrefix(String serverInfo) {
		String prefix = null;
		
		if(serverInfo.contains("WildFly")) {
			prefix = "java:jboss/datasources";
			
		} else if(serverInfo.contains("Apache Tomcat")) {
			prefix = "java:comp/env/jdbc";
			
		} else {
			prefix = "java:";
			
		}
		
		return prefix;
	}
	
	@Override
	public DataSource getDataSource(String prefix, String ds) {
		return getDataSourceFromJNDI(prefix + "/pls/" + ds);
	}
	
	private static DataSource getDataSourceFromJNDI(String name) {
		try {
			
			InitialContext ic = new InitialContext();
			return (DataSource) ic.lookup(name);
			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
