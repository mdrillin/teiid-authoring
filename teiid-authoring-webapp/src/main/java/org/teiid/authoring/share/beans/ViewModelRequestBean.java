package org.teiid.authoring.share.beans;

/**
 * ViewModelRequest
 * Object for passing the requested view model properties
 *
 */
public class ViewModelRequestBean {
	private String name;
	private String description;
	private String ddl;
	private boolean isVisible = false;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDdl() {
		return ddl;
	}
	public void setDdl(String ddl) {
		this.ddl = ddl;
	}
	public boolean isVisible() {
		return isVisible;
	}
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}	
}
