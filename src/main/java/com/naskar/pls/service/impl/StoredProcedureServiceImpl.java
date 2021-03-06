package com.naskar.pls.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.naskar.pls.service.Holder;
import com.naskar.pls.service.RequestParameters;
import com.naskar.pls.service.SessionAttributes;
import com.naskar.pls.service.StoredProcedureService;
import com.naskar.pls.util.FileDeleteOnCloseInputStream;

import oracle.jdbc.OracleTypes;

public class StoredProcedureServiceImpl implements StoredProcedureService {
	
	private static final String PARAM_SESSION = "pls_session_";
	
	private interface Action<T> {
		void call(T t) throws Exception;
	}
	
	private Logger logger;
	private SimpleDateFormat sdf;
	
	public StoredProcedureServiceImpl() {
		this.logger = Logger.getLogger(this.getClass());
		this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	}
	
	@Override
	public Map<String, Object> execute(DataSource ds, String procedureName, 
			RequestParameters params, SessionAttributes session) {
		
		final Map<String, Object> result = new HashMap<String, Object>();
		
		Connection conn = null;
		try {
			conn = ds.getConnection();
			conn.setAutoCommit(false);
						
			List<Action<CallableStatement>> actionsIn = new ArrayList<Action<CallableStatement>>();
			List<Action<CallableStatement>> actionsOut = new ArrayList<Action<CallableStatement>>();
			Holder<Boolean> hasReturn = new Holder<Boolean>(false);
			
			int size = createActions(conn, procedureName, params, session, result, actionsIn, actionsOut, hasReturn);
			executeCallable(conn, procedureName, size, actionsIn, actionsOut, hasReturn.get());
			
			conn.commit();
		} catch(Exception e) {
			logger.error("Erro ao executar stored procedure: " + procedureName + ":" + params, e);
			
			if(conn != null) {
				try {
					conn.rollback();
				} catch(Exception er) {
					logger.error("Erro ao efetuar rollback.", er);
				}
			}
			
			throw new RuntimeException(e);
			
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch(Exception e) {
					logger.error("Erro ao fechar connection.", e);
				}
			}
		}
		
		return result;
	}

	private void executeCallable(Connection conn, String procedureName, int size, 
			List<Action<CallableStatement>> actionsIn,
			List<Action<CallableStatement>> actionsOut,
			boolean hasReturn
			) throws SQLException, Exception {
		CallableStatement cs = null;
		
		try {
			int realSize = hasReturn ? size - 1: size;
			cs = conn.prepareCall(
				"{ " + (hasReturn ? "? =" : "") + " call " + procedureName + createParameters(realSize) + " }");
			
			for(Action<CallableStatement> action : actionsIn) {
				action.call(cs);
			}
			
			cs.execute();
			
			for(Action<CallableStatement> action : actionsOut) {
				action.call(cs);
			}
		
		} finally {
			if(cs != null) {
				try {
					cs.close();
				} catch(Exception e) {
					logger.error("Erro ao fechar callableStatement.", e);
				}
			}
		}
	}

	private int createActions(
			Connection conn, 
			String procedureName, 
			final RequestParameters params, 
			final SessionAttributes session, 
			final Map<String, Object> result,
			List<Action<CallableStatement>> actionsIn, 
			List<Action<CallableStatement>> actionsOut,
			Holder<Boolean> hasReturn)
					throws SQLException {
		
		ResultSet rs = null;
		try {
			rs = getProcedureMetadata(procedureName, conn);
			
			int i = 0;
			while (rs.next()) {
				
				final int j = ++i;
				
				String nameTmp = toLowerCaseNullable(rs.getString("COLUMN_NAME"));
				short type = rs.getShort("COLUMN_TYPE");
				int dataType = rs.getInt("DATA_TYPE");
				String dataTypeName = rs.getString("TYPE_NAME");
				
				if(type == DatabaseMetaData.procedureColumnReturn) {
					hasReturn.set(Boolean.TRUE);
					nameTmp = "return";
				}
				
				String name = nameTmp;
				
				if(type == DatabaseMetaData.procedureColumnIn
						|| type == DatabaseMetaData.procedureColumnInOut) {
					
					if(name.startsWith(PARAM_SESSION)) {
						actionsIn.add((cs) -> {
							setValue(cs, j, dataType, session.get(name.substring(PARAM_SESSION.length())));
						});
						
					} else {
						
						if("BLOB".equals(dataTypeName)) {
							actionsIn.add((cs) -> {
								setValue(cs, j, Types.BLOB, params.get(name));
							});
							
						} else {
							actionsIn.add((cs) -> {
								setValue(cs, j, dataType, params.get(name));
							});	
						}
						
						
					}
				}
				
				if(type == DatabaseMetaData.procedureColumnInOut 
						|| type == DatabaseMetaData.procedureColumnOut
						|| type == DatabaseMetaData.procedureColumnReturn) {
					
					if(name.startsWith(PARAM_SESSION)) {
						actionsIn.add((cs) -> {
							cs.registerOutParameter(j, dataType);
						});
						actionsOut.add((cs) -> {
							session.put(name.substring(PARAM_SESSION.length()), getValue(cs, j, dataType));
						});
						
						
					} else {
					
						if("REF CURSOR".equals(dataTypeName)) {
							actionsIn.add((cs) -> {
								cs.registerOutParameter(j, OracleTypes.CURSOR);
							});
							actionsOut.add((cs) -> {
								result.put(name, getResultSet(cs, j));
							});
							
						} else if("BLOB".equals(dataTypeName)) {
							actionsIn.add((cs) -> {
								cs.registerOutParameter(j, OracleTypes.BLOB);
							});
							actionsOut.add((cs) -> {
								result.put(name, getValue(cs, j, Types.BLOB));
							});
							
						} else {
							actionsIn.add((cs) -> {
								cs.registerOutParameter(j, dataType);
							});
							actionsOut.add((cs) -> {
								result.put(name, getValue(cs, j, dataType));
							});
							
						}
					}
					
				}
			}
			
			return i;
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					logger.error("Erro ao fechar resultSet.", e);
				}
			}
		}
	}
	
	private String toLowerCaseNullable(String v) {
		String value = v;
		if(value != null) {
			value = value.toLowerCase();
		}
		return value;
	}

	private List<Map<String, Object>> getResultSet(CallableStatement cs, int j) throws SQLException {
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		
		ResultSet rs = null;
		try {
			rs = (ResultSet)cs.getObject(j);
			
			ResultSetMetaData md = rs.getMetaData();
			while(rs.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				for(int ci = 1; ci <= md.getColumnCount(); ci++) {
					setValueOnMap(md, rs, ci, row);
				}
				l.add(row);
			}
			
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {
					logger.error("Erro ao fechar cursor resultSet.", e);
				}
			}
		}
		
		return l;
	}
	
	private void setValueOnMap(ResultSetMetaData md, ResultSet rs, int j,
			Map<String, Object> map) throws SQLException {
		
		String name = md.getColumnName(j).toLowerCase();
		int columnType = md.getColumnType(j);
		
		switch (columnType) {
			case Types.DECIMAL:
				map.put(name, rs.getDouble(j));
				break;
				
			case Types.NUMERIC:
				map.put(name, rs.getInt(j));
				break;
				
			case Types.TIMESTAMP:
				map.put(name, fromTimestamp(rs.getTimestamp(j)));
				break;
				
			default:
				String value = rs.getString(j); 
				value = value != null ? value.trim() : null;
				map.put(name, value);
				break;
		}
	}

	private Object getValue(CallableStatement cs, int j, int dataType) throws Exception {
		Object o = null;
		switch (dataType) {
			case Types.TIMESTAMP:
				o = fromTimestamp(cs.getTimestamp(j));
				break;
				
			case Types.BLOB:
				o = fromBlob(cs.getBlob(j));
				break;
	
			default:
				o = cs.getObject(j);
				break;
		}
		return o;
		
	}
	
	private Object fromBlob(Blob b) throws Exception {
		File file = File.createTempFile("wsplsblob", "");
		
		InputStream in = b.getBinaryStream();
		FileOutputStream out = new FileOutputStream(file);
		IOUtils.copy(in, out);
		out.close();
		in.close();
		b.free();
		
		return new FileDeleteOnCloseInputStream(file);
	}

	private Object fromTimestamp(Timestamp t) {
		Object o = null;
		if(t != null) {
			o = sdf.format(new Date(t.getTime()));
		} else {
			o = null;
		}
		return o;
	}

	
	private void setValue(CallableStatement cs, int i, int dataType, Object value) throws Exception {
		if(value == null || value.toString().isEmpty()) {
			cs.setNull(i, dataType);
			
		} else {
			
			switch (dataType) {
				case Types.VARCHAR:
					cs.setString(i, String.valueOf(value));
					break;
					
				case Types.DECIMAL:
					cs.setDouble(i, new Double(value.toString()));
					break;
					
				case Types.NUMERIC:
					cs.setInt(i, new Integer(value.toString()));
					break;
					
				case Types.TIMESTAMP:
					cs.setTimestamp(i, new Timestamp(sdf.parse(value.toString()).getTime()));
					break;
					
				case Types.BLOB:
					cs.setBlob(i, createBlob(cs, value));
					break;
					
				default:
					cs.setObject(i, value);
					break;
			}
		}
	}
	
	private Blob createBlob(CallableStatement cs, Object value) throws Exception {
		Blob b = cs.getConnection().createBlob();
		OutputStream out = b.setBinaryStream(1L);
		IOUtils.copy((InputStream)value, out);
		out.close();
		return b;
	}

	private String createParameters(int size) {
		StringBuilder sb = new StringBuilder("");
		
		if(size > 0) {
			sb.append("(");
			for(int i = 0; i < size; i++) {
				if(i > 0) {
					sb.append(",");	
				}
				sb.append("?");
			}
			sb.append(")");
		}
		
		return sb.toString();
	}

	private ResultSet getProcedureMetadata(String name, Connection conn) throws SQLException {
		String schemaName = "";
		String packageName = "";
		String procedureName = "";
		
		String[] names = name.split("\\.");
		
		if(names.length > 0) {
			procedureName = names[names.length-1].toUpperCase();
		}
		
		if(names.length > 1) {
			packageName = names[names.length-2].toUpperCase();
		}
		
		if(names.length > 2) {
			schemaName = names[names.length-3].toUpperCase();
		}

		DatabaseMetaData metadata = conn.getMetaData();
		return metadata.getProcedureColumns(packageName, schemaName, procedureName, "%");
	}
	
}
