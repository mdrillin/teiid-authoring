package org.teiid.authoring.client.dialogs;

public class DialogEvent {
	
	private UIEventType type;
	
	public DialogEvent(UIEventType type) {
		this.type = type;
	}
	
	public UIEventType getType() {
		return type;
	}
}
