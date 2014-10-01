package org.teiid.authoring.client.widgets;

public class CheckableNameRow {
	
	private String name;
	private boolean isChecked = false;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
