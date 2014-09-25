package org.teiid.authoring.client.utils;

import java.util.List;


public class DdlHelper {

	/**
	 * Get the View DDL for the supplied params
	 * @param viewName the view name
	 * @param sourceName the source name
	 * @param columnNames the list of column names
	 * @return the View DDL
	 */
	public static String getViewDdl(String viewName, String sourceName, List<String> columnNames) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE VIEW ");
		sb.append(viewName);
		sb.append(" AS SELECT ");
		sb.append(getColString(columnNames));
		sb.append(" FROM ");
		sb.append(sourceName);
		sb.append(";");
		
		return sb.toString();
	}
	
	/**
	 * Generated View DDL that supports the Teiid OData requirement - that views must have a Primary Key - to get auto-generated.
	 * @param viewName the view name
	 * @param sourceName the source name
	 * @param columnNames the list of column names
	 * @return the View DDL
	 */
	public static String getODataViewDdl(String viewName, String sourceName, List<String> columnNames, List<String> typeNames) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE VIEW ");
		sb.append(viewName);
		sb.append(" (RowId integer PRIMARY KEY, ");
		sb.append(getColWithTypeString(columnNames,typeNames));
		sb.append(") AS SELECT ");
		sb.append(" ROW_NUMBER() OVER (ORDER BY ");
		sb.append(columnNames.get(0));
		sb.append(") , ");
		sb.append(getColString(columnNames));
		sb.append(" FROM ");
		sb.append(sourceName);
		sb.append(";");
		
		return sb.toString();
	}

	private static String getColString(List<String> columnNames) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<columnNames.size(); i++) {
			if(i!=0 ) {
				sb.append(",");
			}
			sb.append(columnNames.get(i));
		}
		return sb.toString();
	}
	
	private static String getColWithTypeString(List<String> columnNames, List<String> typeNames) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<columnNames.size(); i++) {
			if(i!=0 ) {
				sb.append(",");
			}
			sb.append(columnNames.get(i));
			sb.append(" ");
			sb.append(typeNames.get(i));
		}
		return sb.toString();
	}
	
	/**
	 * Test if supplied string is null or zero length
	 * @param text
	 */
	private static boolean isEmpty(String text) {
		return (text == null || text.length() == 0);
	}
	
}
