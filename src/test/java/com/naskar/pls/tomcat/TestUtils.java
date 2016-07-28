package com.naskar.pls.tomcat;

import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

public class TestUtils {
	
	private static boolean configured = false;
	
	public static void configureDataSourceTest() {
		if(!configured) { 
			configured = true;
			
			try {
	            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
	            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");      
	            
	            InitialContext ic = new InitialContext();
	
	            ic.createSubcontext("java:");
	            ic.createSubcontext("java:/comp");
	            ic.createSubcontext("java:/comp/env");
	            ic.createSubcontext("java:/comp/env/jdbc");
	            ic.createSubcontext("java:/comp/env/jdbc/pls");
	            
	            Properties p = readProperties();
	           
	            BasicDataSource ds = new BasicDataSource();
	            ds.setUrl(p.getProperty("url"));
	            ds.setUsername(p.getProperty("username"));
	            ds.setPassword(p.getProperty("password"));
	            
	            ic.bind("java:/comp/env/jdbc/pls/test", ds);
	            
	        } catch (Exception ex) {
	            throw new RuntimeException(ex);
	        }
		}
	}
	
	private static Properties readProperties() throws IOException {
		Properties p = new Properties();
		p.load(TestUtils.class.getResourceAsStream("db.properties"));
		return p;
	}

}
