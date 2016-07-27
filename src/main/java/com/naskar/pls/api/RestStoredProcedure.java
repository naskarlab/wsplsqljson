package com.naskar.pls.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.naskar.pls.service.DataSourceService;
import com.naskar.pls.service.StoredProcedureService;
import com.naskar.pls.service.impl.DataSourceServiceImpl;
import com.naskar.pls.service.impl.SessionAttributesImpl;
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
    				values, new SessionAttributesImpl(request));
    		
    		return Response.ok(result)
    				.type(MediaType.APPLICATION_JSON)
    				.build();
	    	
    	} catch(Throwable t) {
    		
    		return Response.serverError()
    			.entity(error("Erro ao executar: " + name, t))
    			.type(MediaType.TEXT_PLAIN)
    			.build();
    		
    	}
    }
    
    private static Throwable getRootCause(Throwable e) {
		
		// seguranca, caso encontre exception mal 'comportadas'
		int depth = 1000000;
		
		Throwable root = e;
		while(root.getCause() != null && depth > 0) {
			root = root.getCause();
			depth--;
		}
		
		return root;
	}
	
	private String generateLogError(String msg, Throwable t) {
		// TODO: criar env para debugMode e tirar a causa
		return msg + " - Códido do erro: " + random.nextInt(9999) + ", causa:" + getRootCause(t).getMessage();
	}
	
	private String error(String msg, Throwable t) {
		String errorMsg = generateLogError(msg, t);
		logger.error(errorMsg, t);
		return errorMsg;
	}
    
}
