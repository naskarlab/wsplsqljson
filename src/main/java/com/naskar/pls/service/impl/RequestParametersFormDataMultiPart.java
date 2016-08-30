package com.naskar.pls.service.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.naskar.pls.service.RequestParameters;

public class RequestParametersFormDataMultiPart implements RequestParameters {
	
	private Map<String, Object> params;
	
	public RequestParametersFormDataMultiPart(FormDataMultiPart values) {
		this.params = getMap(values);
	}
	
	@Override
	public Object get(String name) {
		return this.params.get(name);
	}
	
	private static Map<String, Object> getMap(FormDataMultiPart values) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, List<FormDataBodyPart>> m = values.getFields();
		for(String key : m.keySet()) {
			
			FormDataBodyPart bodyPart = m.get(key).get(0);
			String name = bodyPart.getFormDataContentDisposition().getFileName();
			if(name != null) {
				params.put(key.toLowerCase(), bodyPart.getEntityAs(InputStream.class));
				
			} else {
				params.put(key.toLowerCase(), bodyPart.getValue());
			}
			
		}
		return params;
	}
	
}
