package org.teiid.authoring.client.widgets;

import org.jboss.errai.databinding.client.api.Bindable;

@Bindable
public class ServiceRow {
	
	private String name;
	private String description;
	private boolean isChecked = false;
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
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public boolean isVisible() {
		return isVisible;
	}
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

}
