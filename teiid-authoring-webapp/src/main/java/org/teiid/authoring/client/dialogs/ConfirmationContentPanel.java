package org.teiid.authoring.client.dialogs;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

@Templated("./ConfirmationContentPanel.html")
public class ConfirmationContentPanel extends Composite {

	private UIEventType okType;
	private UIEventType cancelType;
	private HTMLPanel htmlPanel;
	
    @Inject @DataField("label-title")
    protected Label title;

    @Inject @DataField("panel-message")
    protected VerticalPanel messagePanel;
    
    @Inject @DataField("btn-ok")
    protected Button okButton;
    
    @Inject @DataField("btn-cancel")
    protected Button cancelButton;
    
    @Inject Event<DialogEvent> okEvent;
    
    @Inject Event<DialogEvent> cancelEvent;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    }
    
    public void setTitle(String title) {
    	this.title.setText(title);
    }
    
    public void setMessage(String message) {
    	if(htmlPanel!=null) {
    		messagePanel.remove(0);
    	}
        htmlPanel = new HTMLPanel("<p>"+message+"</p>");
        messagePanel.add(htmlPanel);
    }
    
    public void setOkCancelEventTypes(UIEventType okType, UIEventType cancelType) {
    	this.okType = okType;
    	this.cancelType = cancelType;
    }
    
    /**
     * Event handler that fires when the user clicks the OK button.
     * @param event
     */
    @EventHandler("btn-ok")
    public void onOkButtonClick(ClickEvent event) {
    	okEvent.fire(new DialogEvent(this.okType));
    }
    
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	cancelEvent.fire(new DialogEvent(this.cancelType));
    }
       
}