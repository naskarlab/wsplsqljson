package com.naskar.pls.service;

import javax.sql.DataSource;

public interface DataSourceService {

	String definePrefix(String serverInfo);

	DataSource getDataSource(String prefix, String ds);
}
