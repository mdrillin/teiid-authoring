package org.teiid.authoring.client.dialogs;

public class UiEvent {
	
	private UiEventType type;
	
	public UiEvent(UiEventType type) {
		this.type = type;
	}
	
	public UiEventType getType() {
		return type;
	}
}
