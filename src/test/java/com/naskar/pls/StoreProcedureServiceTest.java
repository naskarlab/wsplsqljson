package com.naskar.pls;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;
import com.naskar.pls.tomcat.EmbeddedTomcatServerBaseTest;

import net.minidev.json.JSONArray;

public class StoreProcedureServiceTest /*extends EmbeddedTomcatServerBaseTest*/ {
	
	public static String getServerUrl() {
    	return "http://localhost:8080/wspls";
    }

	@Test
	public void testSucessPkgTestPrcTeste() throws Exception {
		
    	String result =
    		Request.Post(getServerUrl() + "/api/test/tt.pkg_teste.prc_teste")
	        	.bodyForm(Form.form()
	        		.add("p_id",  "21")
	        		.add("p_name",  "rafael")
	        		.add("p_date",  "26/05/2016 23:59:59")
	        		.add("p_dados",  "1")
	        		.build())
	        	.execute().returnContent().asString();
    	
        Integer p_dados = JsonPath.read(result, "$.p_dados");
    	Integer p_result_id = JsonPath.read(result, "$.p_result_id");
    	String p_result_name = JsonPath.read(result, "$.p_result_name");
    	String p_result_date = JsonPath.read(result, "$.p_result_date");
    	Double p_result_2 = JsonPath.read(result, "$.p_result_2");
    	String p_result_3 = JsonPath.read(result, "$.p_result_3");
        
        assertThat(p_dados, notNullValue());
        assertThat(p_result_id, notNullValue());
        assertThat(p_result_name, notNullValue());
        assertThat(p_result_date, notNullValue());
        assertThat(p_result_2, notNullValue());
        assertThat(p_result_3, nullValue());
	}
	
	@Test(expected=HttpResponseException.class)
	public void testFailPkgTestPrcTeste() throws Exception {
		Request.Post(getServerUrl() + "/api/test/tt.pkg_teste.prc_teste")
        	.bodyForm(Form.form()
        		.add("p_id",  "21A")
        		.add("p_name",  "rafael")
        		.add("p_date",  "26/05/2016 23:59:59")
        		.add("p_dados",  "1")
        		.build())
        	.execute().returnContent();
	}
	
	@Test
	public void testSucessPkgTesteFncTesteRefcursor() throws Exception {
    	String result =
    		Request.Post(getServerUrl() + "/api/test/tt.pkg_teste.fnc_teste_refcursor")
	        	.bodyForm(Form.form()
	        		.add("p_id",  "21")
	        		.build())
	        	.execute().returnContent().asString();
    	
    	JSONArray returnRows = JsonPath.read(result, "$.return");
        
    	assertThat(returnRows.size(), equalTo(3));
	}

}
