package com.naskar.pls;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;
import com.naskar.pls.tomcat.EmbeddedTomcatServerBaseTest;

public class StoreProcedureServiceMultipartFormDataTest extends EmbeddedTomcatServerBaseTest {

	@Test
	public void testSuccessUploadFileToBlob() throws Exception {
		
		String filepath = this.getClass().getResource("/").getFile() + "/hello-world.pdf";
		File file = new File(filepath);
		Long expectedSize = FileUtils.sizeOf(file);
		
    	String result =
    		Request.Post(getServerUrl() + "/api/test/tt.pkg_blob.prc_upload_blob")
	        	.body(MultipartEntityBuilder
        		    .create()
        		    .addTextBody("p_id_doc", "1234")
        		    .addBinaryBody("p_file1", file, 
        		    	ContentType.create("application/octet-stream"), file.getName())
        		    .build())
	        	.execute().returnContent().asString();
    	
        Integer actualSize = JsonPath.read(result, "$.p_size_file1");
        
        assertEquals(new Integer(expectedSize.intValue()), actualSize);
	}
	
	@Test
	public void testSuccessDownloadFileFromBlob() throws Exception {
		
		String filepath = this.getClass().getResource("/").getFile() + "/hello-world.pdf";
		File file = new File(filepath);
		String expected = FileUtils.readFileToString(file);
		
		Response response = 
			Request.Post(getServerUrl() + "/api/test/tt.pkg_blob.prc_download_blob")
	        	.body(MultipartEntityBuilder
	        		    .create()
	        		    .addTextBody("p_id_doc", "100")
	        		    .build())
		        	.execute();
		
    	String actual = response.returnContent().asString(Charset.forName("UTF-8"));
    	
        assertEquals(expected, actual);
	}
	
	@Test
	public void testSuccessDownloadFileFromBlobPostForm() throws Exception {
		
		String filepath = this.getClass().getResource("/").getFile() + "/hello-world.pdf";
		File file = new File(filepath);
		String expected = FileUtils.readFileToString(file);
		
		Response response = 
			Request.Post(getServerUrl() + "/api/test/tt.pkg_blob.prc_download_blob")
				.bodyForm(Form.form()
	        		.add("p_id_doc", "100")
	        		.build())
		        .execute();
		
    	String actual = response.returnContent().asString(Charset.forName("UTF-8"));
    	
        assertEquals(expected, actual);
	}
}
