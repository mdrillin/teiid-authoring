package org.teiid.authoring.client.dialogs;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

@Templated("./StatusPopupPanel.html")
public class StatusPopupPanel extends Composite {

	private HTMLPanel htmlPanel;
	
    @Inject @DataField("label-title")
    protected Label title;

    @Inject @DataField("panel-message")
    protected VerticalPanel messagePanel;
    
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
           
}