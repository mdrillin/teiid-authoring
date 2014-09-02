package org.teiid.authoring.share.beans;

import java.util.Comparator;


/*
 * Comparator for ordering the PropertyBeans
 */
public class PropertyBeanComparator implements Comparator<DataSourcePropertyBean> {

	boolean ascending = true;
	
	public PropertyBeanComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	public int compare(DataSourcePropertyBean s1, DataSourcePropertyBean s2) {
		String s1Name = s1.getName();
		String s2Name = s2.getName();
		
		if(ascending) {
			return s1Name.compareToIgnoreCase(s2Name);
		}
		return s2Name.compareToIgnoreCase(s1Name);
	}        

}
