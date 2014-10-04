package org.teiid.authoring.client.dialogs;

import org.kie.uberfire.client.common.Popup;

import com.google.gwt.user.client.ui.Widget;

/**
 * Popup which shows a title and status message
 */
public class StatusPopup extends Popup {

	private StatusPopupPanel contentPanel;

    public StatusPopup(final StatusPopupPanel contentPanel, final String title) {
        setup( contentPanel, title);
    }

    protected void setup(final StatusPopupPanel contentPanel, final String title) {
    	this.contentPanel = contentPanel;
    	
        setModal( true );
        
        setTitle(title);
    }

    @Override
    public Widget getContent() {
        return this.contentPanel;
    }
    
    public void setContentTitle(String title) {
    	this.contentPanel.setTitle(title);
    }

    public void setContentMessage(String message) {
    	this.contentPanel.setMessage(message);
    }
    
    public void setSize(int width, int height) {
    	//this.contentPanel.setSize(width+"px", height+"px");
    }
}
