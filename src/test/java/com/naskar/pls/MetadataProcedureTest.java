package com.naskar.pls;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.junit.Test;

public class MetadataProcedureTest {

	// @Test
	public void test() throws Exception {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.32.137:1521:XE", "tt", "tt");

		DatabaseMetaData metadata = conn.getMetaData();

		String schemaName = "";
		String packageName = "PKG_TESTE";
		String procedureName = "PRC_TESTE";

		ResultSet rs = metadata.getProcedureColumns(packageName, schemaName, procedureName, "%");

		while (rs.next()) {
			// get stored procedure metadata
			String procedureCatalog = rs.getString(1);
			String procedureSchema = rs.getString(2);
			procedureName = rs.getString(3);
			String columnName = rs.getString(4);
			short columnReturn = rs.getShort(5);
			int columnDataType = rs.getInt(6);
			String columnReturnTypeName = rs.getString(7);
			int columnPrecision = rs.getInt(8);
			int columnByteLength = rs.getInt(9);
			short columnScale = rs.getShort(10);
			short columnRadix = rs.getShort(11);
			short columnNullable = rs.getShort(12);
			String columnRemarks = rs.getString(13);

			System.out.println("---------------------------");
			//System.out.println("stored Procedure name=" + procedureName);
			//System.out.println("procedureCatalog=" + procedureCatalog);
			//System.out.println("procedureSchema=" + procedureSchema);
			//System.out.println("procedureName=" + procedureName);
			System.out.print(" columnName=" + columnName);
			System.out.print(" columnReturn=" + columnReturn);
			System.out.print(" columnDataType=" + columnDataType);
			System.out.print(" columnReturnTypeName=" + columnReturnTypeName);
			System.out.print(" columnPrecision=" + columnPrecision);
			System.out.print(" columnByteLength=" + columnByteLength);
			System.out.print(" columnScale=" + columnScale);
			System.out.print(" columnRadix=" + columnRadix);
			System.out.print(" columnNullable=" + columnNullable);
			System.out.println(" columnRemarks=" + columnRemarks);			
		}
	}

}
