package com.naskar.pls.service;

import java.util.Map;

import javax.sql.DataSource;
import javax.ws.rs.core.MultivaluedMap;

public interface StoredProcedureService {

	Map<String, Object> execute(DataSource ds, String procedureName, MultivaluedMap<String, String> values,
			SessionAttributes session);
}
