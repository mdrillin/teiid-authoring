package org.teiid.authoring.client.dialogs;

import java.util.List;

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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

@Templated("./AddViewSourceContentPanel.html")
public class AddViewSourceContentPanel extends Composite {

	private UiEventType okType;
	private UiEventType cancelType;
	private HTMLPanel htmlPanel;
	
    @Inject @DataField("label-title")
    protected Label title;

    @Inject @DataField("panel-message")
    protected VerticalPanel messagePanel;
    
    @Inject @DataField("listbox-all-sources")
    protected ListBox allSourcesListBox;
    
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
    
    public void setAllAvailableSources(List<String> allSources) {
    	populateListBox(allSources);
    }
    
    /**
     * Init the List of Available sources
     */
    private void populateListBox(List<String> allSources) {
    	// Make sure clear first
    	allSourcesListBox.clear();

    	for(int i=0; i<allSources.size(); i++) {
    		allSourcesListBox.insertItem(allSources.get(i), i);
    	}
    	
    	// Initialize by setting the selection to the first item.
    	allSourcesListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected data source from the listbox
     * @return
     */
    public String getSelectedSource() {
    	int index = allSourcesListBox.getSelectedIndex();
    	return allSourcesListBox.getValue(index);
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