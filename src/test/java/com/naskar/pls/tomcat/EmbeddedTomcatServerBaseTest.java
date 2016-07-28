package com.naskar.pls.tomcat;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EmbeddedTomcatServerBaseTest {
	
	protected static final String baseDir = 
		EmbeddedTomcatServerBaseTest.class.getResource("/").getFile().toString();
	
	private static Tomcat tomcat;
	
    public static String getServerUrl() {
    	return "http://localhost:8021/tt";
    }
    
	@BeforeClass
    public static void setUpClass() throws Exception {
		tomcat = new Tomcat();
		tomcat.setPort(8021);
				
		Context ctx = tomcat.addWebapp("/tt", baseDir + "../../src/main/webapp");
		File configFile = new File(baseDir + "/META-INF/context.xml");
		ctx.setConfigFile(configFile.toURI().toURL());
		
		tomcat.enableNaming();
		
		tomcat.start();
    }
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if (tomcat != null) {
			try {
				tomcat.getService().stop();
				tomcat.getServer().stop();
				tomcat.stop();
				tomcat.destroy();
				tomcat = null;
			} catch(Exception e) {
			}
		}
	}
	
	@Test
    public void pingTomcatTest() throws Exception {
		URL obj = new URL(getServerUrl());
		HttpURLConnection con = (HttpURLConnection)obj.openConnection();
 
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
 		assertThat(con.getResponseCode(), equalTo(200));
 		
 		con.disconnect();
	}
	

}
