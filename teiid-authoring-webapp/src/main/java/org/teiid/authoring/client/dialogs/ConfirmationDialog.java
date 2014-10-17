package org.teiid.authoring.client.dialogs;

import org.kie.uberfire.client.common.Popup;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This builds on the FormStyleLayout for providing common popup features in a
 * columnar form layout, with a title and a large (ish) icon.
 */
public class ConfirmationDialog extends Popup {

	private ConfirmationContentPanel contentPanel;

    public ConfirmationDialog(final ConfirmationContentPanel contentPanel, final String title) {
        setup( contentPanel, title);
    }

    protected void setup(final ConfirmationContentPanel contentPanel, final String title) {
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
    
    public void setContentPanel(VerticalPanel panel) {
    	this.contentPanel.addContentPanel(panel);
    }
    
    public void setOkCancelEventTypes(UiEventType okType, UiEventType cancelType) {
    	this.contentPanel.setOkCancelEventTypes(okType, cancelType);
    }
    
    public void setSize(int width, int height) {
    	//this.contentPanel.setSize(width+"px", height+"px");
    }
}
