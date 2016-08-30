package com.naskar.pls.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.naskar.pls.service.DataSourceService;
import com.naskar.pls.service.StoredProcedureService;
import com.naskar.pls.service.impl.DataSourceServiceImpl;
import com.naskar.pls.service.impl.RequestParametersFormDataMultiPart;
import com.naskar.pls.service.impl.RequestParametersMultivaluedMap;
import com.naskar.pls.service.impl.SessionAttributesHttpServlet;
import com.naskar.pls.service.impl.StoredProcedureServiceImpl;

@Singleton
@Path("/")
public class RestStoredProcedure {
	
	private Logger logger;
	private Random random;
	
	private StoredProcedureService storedProcedureService;
	private DataSourceService dataSourceService;
	
	public RestStoredProcedure() {
		this.logger = Logger.getLogger(this.getClass());
		this.storedProcedureService = new StoredProcedureServiceImpl();
		this.dataSourceService = new DataSourceServiceImpl();
		this.random = new Random(new Date().getTime());
	}
	
	@POST
    @Path("/{ds}/{name}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response prc(
    		@Context HttpServletRequest request,
    		@PathParam("ds") String ds,
    		@PathParam("name") String name, 
    		MultivaluedMap<String, String> values) {
		
    	try {
    		String prefix = this.dataSourceService.definePrefix(
    			request.getServletContext().getServerInfo());
    		
    		Map<String, Object> result = 
    			storedProcedureService.execute(
    				dataSourceService.getDataSource(prefix, ds), name, 
    				new RequestParametersMultivaluedMap(values), 
    				new SessionAttributesHttpServlet(request));
    		
    		return createResponse(result);
	    	
    	} catch(Throwable t) {
    		
    		return Response.serverError()
    			.entity(error("Erro ao executar: " + name, t))
    			.type(MediaType.TEXT_PLAIN)
    			.build();
    		
    	}
    }
	
	@POST
    @Path("/{ds}/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response prc(
    		@Context HttpServletRequest request,
    		@PathParam("ds") String ds,
    		@PathParam("name") String name, 
    		FormDataMultiPart values) {
    	try {
    		String prefix = this.dataSourceService.definePrefix(
    			request.getServletContext().getServerInfo());
    		
    		Map<String, Object> result = 
    			storedProcedureService.execute(
    				dataSourceService.getDataSource(prefix, ds), name, 
    				new RequestParametersFormDataMultiPart(values), 
    				new SessionAttributesHttpServlet(request));
    		
    		return createResponse(result);
	    	
    	} catch(Throwable t) {
    		
    		return Response.serverError()
    			.entity(error("Erro ao executar: " + name, t))
    			.type(MediaType.TEXT_PLAIN)
    			.build();
    		
    	}
    }

	private Response createResponse(Map<String, Object> result) {
		
		ResponseBuilder builder = null;
		
		if(result.size() == 1) {
			
			Object o = result.values().iterator().next();
			if(o instanceof InputStream) {
				builder = createResponseStream((InputStream)o);
			}
			
		}
		
		if(builder == null) {
			builder = Response.ok(result)
				.type(MediaType.APPLICATION_JSON);
		}
		
		return builder.build();
		
	}

	private ResponseBuilder createResponseStream(final InputStream in) {
		
		return Response.ok(new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) 
					throws IOException, WebApplicationException {
				try {
					
					IOUtils.copy(in, output);
					output.flush();
					
				} finally {
					if(in != null) {
						in.close();
					}
				}
				
			}
			
		}, MediaType.APPLICATION_OCTET_STREAM)
		.header("content-disposition","attachment; filename=arq" + in.hashCode() + ".dat");
	}
	
    private static Throwable getRootCause(Throwable e) {
		
		// seguranca, caso encontre exceptions mal 'comportadas'
		int depth = 1000000;
		
		Throwable root = e;
		while(root.getCause() != null && depth > 0) {
			root = root.getCause();
			depth--;
		}
		
		return root;
	}
	
	private String generateLogError(String msg, Throwable t) {
		String errorMsg = msg + " - Códido do erro: " + random.nextInt(9999);
		
		if(isDebug()) {
			errorMsg += ", causa:" + getRootCause(t).getMessage();
		}
		
		return errorMsg;
	}

	private boolean isDebug() {
		String debug = System.getProperty("wspls.debug", "true"); // enabled default
		return ("false".equalsIgnoreCase(debug));
	}
	
	private String error(String msg, Throwable t) {
		String errorMsg = generateLogError(msg, t);
		logger.error(errorMsg, t);
		return errorMsg;
	}
    
}
