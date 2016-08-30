package com.naskar.pls.service;

import java.util.Map;

import javax.sql.DataSource;

public interface StoredProcedureService {

	Map<String, Object> execute(DataSource ds, String procedureName, 
			RequestParameters params, SessionAttributes session);
}
