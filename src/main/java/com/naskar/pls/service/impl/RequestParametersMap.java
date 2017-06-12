package com.naskar.pls.service.impl;

import java.util.Map;

import com.naskar.pls.service.RequestParameters;

public class RequestParametersMap implements RequestParameters {
	
	private Map<String, Object> params;
	
	public RequestParametersMap(Map<String, Object> values) {
		this.params = values;
	}
	
	@Override
	public Object get(String name) {
		return this.params.get(name);
	}
	
}
