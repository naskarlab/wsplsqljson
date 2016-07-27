package com.naskar.pls;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.junit.Test;

public class StoreProcedureServiceTest {

	@Test
	public void test() throws Exception {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.32.137:1521:XE", "tt", "tt");

		DatabaseMetaData metadata = conn.getMetaData();

		String schemaName = "";
		String packageName = "PKG_TESTE";
		String procedureName = "PRC_TESTE";

		ResultSet rs = metadata.getProcedureColumns(packageName, schemaName, procedureName, "%");

		while (rs.next()) {
			System.out.print(" COLUMN_NAME=" + rs.getString("COLUMN_NAME"));
			System.out.print(" COLUMN_TYPE=" + rs.getShort("COLUMN_TYPE"));
			System.out.print(" DATA_TYPE=" + rs.getInt("DATA_TYPE"));
			System.out.print(" TYPE_NAME=" + rs.getString("TYPE_NAME"));
			System.out.println("");
		}
	}

}
