package org.teiid.authoring.client.widgets;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.services.VdbRpcService;
import org.teiid.authoring.share.beans.Constants;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

@Templated("./LibraryServiceWidget.html")
public class LibraryServiceWidget extends Composite implements HasModel<ServiceRow> {

	private static final String MORE_ACTIONS = "More Actions";
	private static final String EDIT_ACTION = "Edit Service";
	private static final String TEST_ACTION = "Test Service";
	private static final String DELETE_ACTION = "Delete Service";
	
	@Inject
	private PlaceManager placeManager;

    @Inject
    protected VdbRpcService vdbService;
    
	@Inject @AutoBound DataBinder<ServiceRow> serviceBinder;

	@Inject @Bound @DataField("label-servicewidget-name") Label name;

	@Inject @Bound @DataField("label-servicewidget-description") Label description;

	@Inject @DataField("btn-servicewidget-view")
	protected Button viewServiceButton;

    @Inject @DataField("listbox-servicewidget-more-actions")
    protected ListBox moreActionsListBox;
    
	public ServiceRow getModel() {
		return serviceBinder.getModel();
	}

	public void setModel(ServiceRow service) {
		serviceBinder.setModel(service);
	}

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	populateMoreActionsListBox();
    	
        // Change Listener for Type ListBox
    	moreActionsListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String action = getSelectedAction();    
        		if(action.equals(EDIT_ACTION)) {
        			doEditService();
        		} else if(action.equals(TEST_ACTION)) {
        			doViewService();
        		} else if(action.equals(DELETE_ACTION)) {
        			doRemoveService();
        		}
        	}
        });
    }
    
    /**
     * Init the List of Service actions
     */
    private void populateMoreActionsListBox( ) {
    	// Make sure clear first
    	moreActionsListBox.clear();

    	moreActionsListBox.insertItem(MORE_ACTIONS, 0);
    	moreActionsListBox.insertItem(EDIT_ACTION, 1);
    	moreActionsListBox.insertItem(TEST_ACTION, 2);
    	moreActionsListBox.insertItem(DELETE_ACTION, 3);
    	
    	// Initialize by setting the selection to the first item.
    	moreActionsListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected action from the MoreActions dropdown
     * @return
     */
    private String getSelectedAction() {
    	int index = moreActionsListBox.getSelectedIndex();
    	return moreActionsListBox.getValue(index);
    }
    
	/**
	 * Event handler that fires when the user clicks the ViewService button.
	 * @param event
	 */
	@EventHandler("btn-servicewidget-view")
	public void onViewServiceButtonClick(ClickEvent event) {
		doViewService();
	}

	/**
	 * View Service - transitions to ViewDataServiceScreen
	 */
	protected void doViewService() {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.SERVICE_NAME_KEY, svcName);

		placeManager.goTo(new DefaultPlaceRequest("DataServiceDetailsScreen",parameters));
	}
    
	/**
	 * Edit Service - transitions to EditDataServiceScreen
	 */
	protected void doEditService() {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.SERVICE_NAME_KEY, svcName);

		placeManager.goTo(new DefaultPlaceRequest("EditDataServiceScreen",parameters));
	}
	
    protected void doRemoveService( ) {
		String svcName = getModel().getName();
		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put(Constants.SERVICE_NAME_KEY, svcName);
		
		placeManager.goTo(new DefaultPlaceRequest("DataServicesLibraryScreen",parameters));
    }

}