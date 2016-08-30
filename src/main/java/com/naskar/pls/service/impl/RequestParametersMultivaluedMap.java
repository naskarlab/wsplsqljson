package com.naskar.pls.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.naskar.pls.service.RequestParameters;

public class RequestParametersMultivaluedMap implements RequestParameters {
	
	private Map<String, Object> params;
	
	public RequestParametersMultivaluedMap(MultivaluedMap<String, String> values) {
		this.params = getMap(values);
	}
	
	@Override
	public Object get(String name) {
		return this.params.get(name);
	}
	
	private static Map<String, Object> getMap(MultivaluedMap<String, String> values) {
		Map<String, Object> params = new HashMap<String, Object>();
		for(String key : values.keySet()) {
			params.put(key.toLowerCase(), values.getFirst(key));
		}
		return params;
	}
	
}
