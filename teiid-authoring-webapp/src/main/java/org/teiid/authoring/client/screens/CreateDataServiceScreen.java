/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.authoring.client.screens;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.dialogs.UiEvent;
import org.teiid.authoring.client.dialogs.UiEventType;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.services.NotificationService;
import org.teiid.authoring.client.services.QueryRpcService;
import org.teiid.authoring.client.services.TeiidRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.client.widgets.ViewEditorPanel;
import org.teiid.authoring.client.widgets.VisibilityRadios;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.NotificationBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * CreateDataServiceScreen - used for creation of Data Services
 *
 */
@Dependent
@Templated("./CreateDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "CreateDataServiceScreen")
public class CreateDataServiceScreen extends Composite {

	private String statusEnterName = null;
	private String statusClickCreate = null;
	
    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("textbox-create-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-create-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("radios-create-service-visibility")
    protected VisibilityRadios serviceVisibleRadios;
    
    @Inject @DataField("label-create-service-status")
    protected Label statusLabel;
 
    @Inject @DataField("view-editor-create-service")
    protected ViewEditorPanel viewEditorPanel;
    
    @Inject @DataField("btn-create-service-create")
    protected Button createServiceButton;
    
    @Inject @DataField("btn-create-service-cancel")
    protected Button cancelButton;
    
    @Override
    @WorkbenchPartTitle
    public String getTitle() {
      return Constants.BLANK;
    }
    
    @WorkbenchPartView
    public IsWidget getView() {
        return this;
    }
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
		statusEnterName = i18n.format("createdataservice.status-label-enter-name");
		statusClickCreate = i18n.format("createdataservice.status-label-click-create");
		
		viewEditorPanel.setTitle(i18n.format("createdataservice.vieweditor-title"));
		viewEditorPanel.setDescription(i18n.format("createdataservice.vieweditor-description"));
		
    	serviceVisibleRadios.setValue(true);
    	
    	serviceNameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	viewEditorPanel.setServiceName(serviceNameTextBox.getText());
            	// Update status
            	updateStatus();
            }
        });
    	    	
    	// Set the initial status
    	updateStatus();
    	
    }
    
    /**
     * Update the screen status.  Ensures that the service name is valid, then checks status
     * of the viewEditor panel.
     */
	private void updateStatus( ) {
    	boolean isOK = true;
    	
    	// Warning for missing service name
    	String serviceName = serviceNameTextBox.getText();
    	if(StringUtils.isEmpty(serviceName)) {
    		statusLabel.setText(statusEnterName);
    		isOK = false;
    	}
    	
		// Check for missing view DDL - if serviceName passed
    	if(isOK) {
    		String viewEditorStatus = viewEditorPanel.getStatus();
    		if(!Constants.OK.equals(viewEditorStatus)) {
    			statusLabel.setText(viewEditorStatus);
    			isOK = false;
    		}
    	}
    	
    	if(isOK) {
    		statusLabel.setText(statusClickCreate);
    		createServiceButton.setEnabled(true);
    	} else {
    		createServiceButton.setEnabled(false);
    	}
    }
	
    /**
     * Handles UiEvents from viewEditorPanel
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// change received from viewEditor
    	if(dEvent.getType() == UiEventType.VIEW_EDITOR_CHANGED) {
    		updateStatus();
    	}
    }

    /**
     * Event handler that fires when the user clicks the Create button.
     * @param event
     */
    @EventHandler("btn-create-service-create")
    public void onPublishServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    /**
     * Create and deploy the Service dynamic VDB
     */
    private void doCreateService() {
    	final String serviceName = this.serviceNameTextBox.getText();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("createdataservice.creating-service-title"), //$NON-NLS-1$
                i18n.format("createdataservice.creating-service-msg", serviceName)); //$NON-NLS-1$
            	
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	final String viewModel = serviceName;
    	String viewDdl = viewEditorPanel.getViewDdl();
    	boolean isVisible = serviceVisibleRadios.isVisibleSelected();
    	List<String> rqdImportVdbNames = viewEditorPanel.getSrcVdbNames();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription(serviceDescription);
    	viewModelRequest.setDdl(viewDdl);
    	viewModelRequest.setVisible(isVisible);
    	viewModelRequest.setRequiredImportVdbNames(rqdImportVdbNames);
    	    	
    	final String svcVdbName = Constants.SERVICE_VDB_PREFIX+serviceName;
    	teiidService.deployNewVDB(svcVdbName, 1, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("createdataservice.creating-service-complete"), //$NON-NLS-1$
                        i18n.format("createdataservice.creating-service-complete-msg")); //$NON-NLS-1$

                cleanupTestVdbs();
            	
            	Map<String,String> parameters = new HashMap<String,String>();
            	parameters.put(Constants.SERVICE_NAME_KEY, viewModel);
            	placeManager.goTo(new DefaultPlaceRequest("DataServiceDetailsScreen",parameters));
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("createdataservice.creating-service-error"), error); //$NON-NLS-1$
//                addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
    
    /**
     * Do a clean-up of any temporary VDBs that may have not been undeployed
     */
 	private void cleanupTestVdbs( ) {
 		teiidService.deleteDynamicVdbsWithPrefix(Constants.SERVICE_TEST_VDB_PREFIX, new IRpcServiceInvocationHandler<Void>() {
    		@Override
    		public void onReturn(Void data) {
    		}
    		@Override
    		public void onError(Throwable error) {
    		}
    	});
 	}
   
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-create-service-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	placeManager.goTo("DataServicesLibraryScreen");
    }
        
}
