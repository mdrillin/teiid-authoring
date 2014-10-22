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

import java.util.ArrayList;
import java.util.Collection;
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
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.NotificationBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * EditDataServiceScreen - used to edit existing Data Services
 *
 */
@Dependent
@Templated("./EditDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "EditDataServiceScreen")
public class EditDataServiceScreen extends Composite {

	private String statusEnterName;
	private String statusClickSave;
	private String serviceOriginalName;
	
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

    @Inject @DataField("textbox-edit-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-edit-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("checkbox-edit-service-visibility")
    protected CheckBox serviceVisibleCheckBox;
    
    @Inject @DataField("label-edit-service-status")
    protected Label statusLabel;
    
    @Inject @DataField("view-editor-edit-service")
    protected ViewEditorPanel viewEditorPanel;
    
    @Inject @DataField("btn-edit-service-save")
    protected Button saveServiceButton;
    
    @Inject @DataField("btn-edit-service-cancel")
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
		statusEnterName = i18n.format("editdataservice.status-label-enter-name");
		statusClickSave = i18n.format("editdataservice.status-label-click-save");
		
		viewEditorPanel.setTitle(i18n.format("editdataservice.vieweditor-title"));
		viewEditorPanel.setDescription(i18n.format("editdataservice.vieweditor-description"));
		serviceVisibleCheckBox.setText(i18n.format("editdataservice.checkbox-service-visible"));
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	String serviceName = place.getParameter(Constants.SERVICE_NAME_KEY, "[unknown]");
    	serviceNameTextBox.setText(serviceName);
    	serviceOriginalName = serviceName;
    	
    	viewEditorPanel.setServiceName(serviceName);
    	serviceNameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	viewEditorPanel.setServiceName(serviceNameTextBox.getText());
            	// Update status
            	updateStatus();
            }
        });
    	    	
    	doGetDataServiceDetails(serviceName);
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
    		statusLabel.setText(statusClickSave);
    		saveServiceButton.setEnabled(true);
    	} else {
    		saveServiceButton.setEnabled(false);
    	}
    }
        
    /**
     * Get the Data Service details to populate the page
     * @param serviceName the name of the service
     */
    protected void doGetDataServiceDetails(final String serviceName) {
    	final String serviceVdb = Constants.SERVICE_VDB_PREFIX+serviceName;
    	teiidService.getVdbDetails(serviceVdb, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
            	Collection<VdbModelBean> vdbModels = vdbDetailsBean.getModels();
            	for(VdbModelBean vdbModel : vdbModels) {
            		if(vdbModel.getName().equals(serviceName)) {
            			String description = vdbModel.getDescription();
            			serviceDescriptionTextBox.setText(description);
            			
            			boolean isVisible = vdbModel.isVisible();
            			serviceVisibleCheckBox.setValue(isVisible);
            			
            			String ddl = vdbModel.getDdl();
            			viewEditorPanel.setViewDdl(ddl);
            		}
            	}
    			List<String> importVdbs = vdbDetailsBean.getImportedVdbNames();
    			List<String> srcNames = new ArrayList<String>(importVdbs.size());
    			for(String importVdbName : importVdbs) {
    				if(importVdbName.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX)) {
    			    	// The source VDB name, but without the prefix
    			    	String srcName = importVdbName.substring(importVdbName.indexOf(Constants.SERVICE_SOURCE_VDB_PREFIX)+Constants.SERVICE_SOURCE_VDB_PREFIX.length());
    					srcNames.add(srcName);
    				}
    			}
    			viewEditorPanel.setViewSources(srcNames);
            	
            	// Set the initial status
            	updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-svc-details"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
            }
        });       
    }
    
    /**
     * Event handler that fires when the user clicks the SaveChanges button.
     * @param event
     */
    @EventHandler("btn-edit-service-save")
    private void onSaveServiceButtonClick(ClickEvent event) {
    	final String serviceName = this.serviceNameTextBox.getText();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("editdataservice.saving-service-title"), //$NON-NLS-1$
                i18n.format("editdataservice.saving-service-msg", serviceName)); //$NON-NLS-1$
            	
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	final String viewModel = serviceName;
    	String viewDdl = viewEditorPanel.getViewDdl();
    	boolean isVisible = serviceVisibleCheckBox.getValue();
    	List<String> rqdImportVdbNames = viewEditorPanel.getViewSourceVdbNames();
    	
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
                        i18n.format("editdataservice.saving-service-complete"), //$NON-NLS-1$
                        i18n.format("editdataservice.saving-service-complete-msg")); //$NON-NLS-1$

                // Delete the original named VDB if there was a rename
                if(!serviceName.equals(serviceOriginalName)) {
                	deleteVdb(Constants.SERVICE_VDB_PREFIX+serviceOriginalName);
                }
                
                // Cleanup test VDBs
                cleanupTestVdbs();
            	
            	Map<String,String> parameters = new HashMap<String,String>();
            	parameters.put(Constants.SERVICE_NAME_KEY, viewModel);
            	placeManager.goTo(new DefaultPlaceRequest("DataServiceDetailsScreen",parameters));
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.saving-service-error"), error); //$NON-NLS-1$
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
     * Deletes the original VDB on a VDB rename operation
     */
 	private void deleteVdb(String vdbName) {
 		teiidService.deleteDataSourceAndVdb(vdbName,vdbName, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> data) {
    		}
    		@Override
    		public void onError(Throwable error) {
    		}
    	});
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
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-edit-service-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	placeManager.goTo("DataServicesLibraryScreen");
    }
        
}
