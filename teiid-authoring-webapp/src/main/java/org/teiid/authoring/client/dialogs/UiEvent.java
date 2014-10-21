package org.teiid.authoring.client.dialogs;

public class UiEvent {
	
	private UiEventType type;
	private String dataSourceName;
	private String dataServiceName;
	
	public UiEvent(UiEventType type) {
		this.type = type;
	}
	
	public UiEventType getType() {
		return type;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	
	public String getDataServiceName() {
		return dataServiceName;
	}

	public void setDataServiceName(String dataServiceName) {
		this.dataServiceName = dataServiceName;
	}
}
