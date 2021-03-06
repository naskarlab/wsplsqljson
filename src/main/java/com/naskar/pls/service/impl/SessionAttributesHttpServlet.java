package com.naskar.pls.service.impl;

import javax.servlet.http.HttpServletRequest;

import com.naskar.pls.service.SessionAttributes;

public class SessionAttributesHttpServlet implements SessionAttributes {
	
	private HttpServletRequest req;
	
	public SessionAttributesHttpServlet(HttpServletRequest req) {
		this.req = req;
	}
	
	@Override
	public Object get(String name) {
		return this.req.getSession(true).getAttribute(name);
	}
	
	@Override
	public void put(String name, Object value) {
		this.req.getSession(true).setAttribute(name, value);
	}
	
}
