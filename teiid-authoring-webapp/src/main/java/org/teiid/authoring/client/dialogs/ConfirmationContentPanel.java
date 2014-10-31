/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	private UiEventType okType;
	private UiEventType cancelType;
	private HTMLPanel htmlPanel;
	
    @Inject @DataField("label-title")
    protected Label title;

    @Inject @DataField("panel-message")
    protected VerticalPanel messagePanel;
    
    @Inject @DataField("btn-ok")
    protected Button okButton;
    
    @Inject @DataField("btn-cancel")
    protected Button cancelButton;
    
    @Inject Event<UiEvent> buttonEvent;
    
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
    
    public void addContentPanel(VerticalPanel panel) {
    	messagePanel.add(panel);
    }
    
    public void setOkCancelEventTypes(UiEventType okType, UiEventType cancelType) {
    	this.okType = okType;
    	this.cancelType = cancelType;
    }
    
    /**
     * Event handler that fires when the user clicks the OK button.
     * @param event
     */
    @EventHandler("btn-ok")
    public void onOkButtonClick(ClickEvent event) {
    	buttonEvent.fire(new UiEvent(this.okType));
    }
    
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	buttonEvent.fire(new UiEvent(this.cancelType));
    }
       
}