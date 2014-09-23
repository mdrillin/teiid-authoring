package org.teiid.authoring.client.utils;

import java.util.List;


public class DdlHelper {

	/**
	 * Get the View DDL for the supplied params
	 * @param viewName the view name
	 * @param viewDescription the view description
	 * @param sourceName the source name
	 * @param columnNames the list of column names
	 * @return the View DDL
	 */
	public static String getViewDdl(String viewName, String viewDescription, String sourceName, List<String> columnNames) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE VIEW ");
		sb.append(viewName);
		if(!isEmpty(viewDescription)) {
			sb.append(" OPTIONS (ANNOTATION '");
			sb.append(viewDescription);
			sb.append("') ");
		}
		sb.append(" AS SELECT ");
		sb.append(getColString(columnNames));
		sb.append(" FROM ");
		sb.append(sourceName);
		sb.append(";");
		
		return sb.toString();
	}

	private static String getColString(List<String> columnNames) {
		StringBuilder sb = new StringBuilder();
		int iCol = 0;
		for(int i=0; i<columnNames.size(); i++) {
			if(i!=0 ) {
				sb.append(",");
			}
			sb.append(columnNames.get(i));
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
