package org.teiid.authoring.share.beans;

import java.util.Comparator;


/*
 * Comparator for ordering the VdbModelBeans
 */
public class VdbModelBeanComparator implements Comparator<VdbModelBean> {

	boolean ascending = true;
	
	public VdbModelBeanComparator(boolean ascending) {
		this.ascending = ascending;
	}
	
	public int compare(VdbModelBean s1, VdbModelBean s2) {
		String s1Name = s1.getName();
		String s2Name = s2.getName();
		
		if(ascending) {
			return s1Name.compareToIgnoreCase(s2Name);
		}
		return s2Name.compareToIgnoreCase(s1Name);
	}        

}
