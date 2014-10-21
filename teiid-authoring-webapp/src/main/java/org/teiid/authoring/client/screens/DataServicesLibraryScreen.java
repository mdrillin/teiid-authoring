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
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.dialogs.ConfirmationContentPanel;
import org.teiid.authoring.client.dialogs.ConfirmationDialog;
import org.teiid.authoring.client.dialogs.UiEvent;
import org.teiid.authoring.client.dialogs.UiEventType;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.services.NotificationService;
import org.teiid.authoring.client.services.QueryRpcService;
import org.teiid.authoring.client.services.TeiidRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.client.widgets.ServiceFlowListWidget;
import org.teiid.authoring.client.widgets.ServiceRow;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.NotificationBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * DataServicesLibraryScreen - shows all published Data Services.
 *
 */
@Dependent
@Templated("./DataServicesLibraryScreen.html#page")
@WorkbenchScreen(identifier = "DataServicesLibraryScreen")
public class DataServicesLibraryScreen extends Composite {

	List<ServiceRow> currentServices = new ArrayList<ServiceRow>();    
	private String deleteServiceName = null;
	
	@Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
	private ConfirmationDialog confirmationDialog;
    @Inject 
    private ConfirmationContentPanel confirmationContent;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
 
    @Inject
    private PlaceManager placeManager;
    
    @Inject ServiceFlowListWidget serviceFlowListWidget;
    
    @Inject @DataField("btn-create-service")
    protected Button createServiceButton;
        
    @Inject @DataField("grid-services")
    protected VerticalPanel servicesPanel;
    
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
    	servicesPanel.add(serviceFlowListWidget);
    	createConfirmDeleteDialog();
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	// Process delete and clone requests from serviceWidget
    	String deleteName = place.getParameter(Constants.DELETE_SERVICE_KEY, "NONE");
    	String cloneName = place.getParameter(Constants.CLONE_SERVICE_KEY, "NONE");
    	if(!deleteName.equals("NONE")) {
    		deleteServiceName = deleteName;
        	String dMsg = i18n.format("dslibrary.confirm-delete-dialog-message",deleteServiceName);
        	confirmationDialog.setContentMessage(dMsg);
    		confirmationDialog.show();
    	} else if(!cloneName.equals("NONE")) {
    		doCloneService(cloneName);
    	} else {
    		doGetServices();
    	}
     	cleanupTestVdbs();
    }
    
    /**
     * Create a dialog for confirming service deletion
     * @param serviceName the name of the service
     */
    private void createConfirmDeleteDialog( ) {
    	String dTitle = i18n.format("dslibrary.confirm-delete-dialog-title");
    	String dMsg = i18n.format("dslibrary.confirm-delete-dialog-message");
    	confirmationDialog = new ConfirmationDialog(confirmationContent, dTitle );
    	confirmationDialog.setContentTitle(dTitle);
    	confirmationDialog.setContentMessage(dMsg);
    	confirmationDialog.setOkCancelEventTypes(UiEventType.DELETE_SERVICE_OK, UiEventType.DELETE_SERVICE_CANCEL);
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source deletion
    	if(dEvent.getType() == UiEventType.DELETE_SERVICE_OK) {
    		confirmationDialog.hide();
    		if(deleteServiceName!=null) {
    			doRemoveService(deleteServiceName);
    		}
        // User has cancelled service deletion
    	} else if(dEvent.getType() == UiEventType.DELETE_SERVICE_CANCEL) {
        	confirmationDialog.hide();
    		doGetServices();
    	// User requesting save service to file
    	} else if(dEvent.getType() == UiEventType.SAVE_SERVICE) {
    		doSaveServiceToFile(dEvent.getDataServiceName());    		
    	}
    }
    
    private void populateGrid(List<ServiceRow> serviceList) {
        serviceFlowListWidget.setItems(serviceList);
    }
    
    /**
     * Get the public services for the supplied VDB
     */
    protected void doGetServices( ) {
    	teiidService.getDynamicVdbsWithPrefix(Constants.SERVICE_VDB_PREFIX, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> serviceVdbs) {
                // Convert VDBDetails to rows for the display
    			List<ServiceRow> serviceTableRows = getServiceRows(serviceVdbs);

    			if(serviceTableRows.isEmpty()) {
    				placeManager.goTo("DataServicesEmptyLibraryScreen");
    			} else {
    		     	populateGrid(serviceTableRows);
    			}
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("dslibrary.fetch-services-error"), error); //$NON-NLS-1$
    		}
    	});
    }
    
   protected void doRemoveService(String serviceName) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("dslibrary.service-deleting"), //$NON-NLS-1$
                i18n.format("dslibrary.service-deleting-msg", serviceName)); //$NON-NLS-1$
        
        String vdbSrcName = Constants.SERVICE_VDB_PREFIX + serviceName;
    	teiidService.deleteDataSourceAndVdb(vdbSrcName, vdbSrcName, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> serviceVdbs) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("dslibrary.delete-success"), //$NON-NLS-1$
                        i18n.format("dslibrary.delete-success-msg")); //$NON-NLS-1$
                
                // Convert VDBDetails to rows for the display
    			List<ServiceRow> serviceTableRows = getServiceRows(serviceVdbs);

    			if(serviceTableRows.isEmpty()) {
    				placeManager.goTo("DataServicesEmptyLibraryScreen");
    			} else {
    		     	populateGrid(serviceTableRows);
    			}
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.completeProgressNotification(notificationBean.getUuid(),
    					i18n.format("dslibrary.service-delete-error"), //$NON-NLS-1$
    					error);
    		}
    	});
    }
    
    protected void doCloneService(String serviceName) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("dslibrary.service-cloning"), //$NON-NLS-1$
                i18n.format("dslibrary.service-cloning-msg", serviceName)); //$NON-NLS-1$
        
        String vdbName = Constants.SERVICE_VDB_PREFIX + serviceName;
        teiidService.cloneDynamicVdbAddSource(vdbName, 1, new IRpcServiceInvocationHandler<List<VdbDetailsBean>>() {
    		@Override
    		public void onReturn(List<VdbDetailsBean> serviceVdbs) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("dslibrary.clone-success"), //$NON-NLS-1$
                        i18n.format("dslibrary.clone-success-msg")); //$NON-NLS-1$
                
                // Convert VDBDetails to rows for the display
    			List<ServiceRow> serviceTableRows = getServiceRows(serviceVdbs);

    			if(serviceTableRows.isEmpty()) {
    				placeManager.goTo("DataServicesEmptyLibraryScreen");
    			} else {
    		     	populateGrid(serviceTableRows);
    			}
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.completeProgressNotification(notificationBean.getUuid(),
    					i18n.format("dslibrary.service-clone-error"), //$NON-NLS-1$
    					error);
    		}
    	});
    }
    
    /**
     * Convert the list of VdbDetailsBeans to ServiceTableRows for the displayer
     * @param serviceVdbs list of service VDB details
     * @return the list of ServiceTableRows
     */
    private List<ServiceRow> getServiceRows(List<VdbDetailsBean> serviceVdbs) {
    	if(serviceVdbs.isEmpty()) return Collections.emptyList();
    	
		List<ServiceRow> serviceRows = new ArrayList<ServiceRow>();
		// Each service VDB contains a single view that represents the service
		for(VdbDetailsBean serviceVdb : serviceVdbs) {
			Collection<VdbModelBean> modelList = serviceVdb.getModels();
			for(VdbModelBean model : modelList) {
				String modelName = model.getName();
				String description = model.getDescription();
				String modelType = model.getType();
				boolean isVisible = model.isVisible();
				if(modelType.equals(Constants.VIRTUAL)) {
					ServiceRow srow = new ServiceRow();
					srow.setName(modelName);
					srow.setDescription(description);
					srow.setVisible(isVisible);
					serviceRows.add(srow);
				}
			}
		}
		return serviceRows;
    }
    
    protected void doSaveServiceToFile(String serviceName) {
        String contentUrl = getWebContext() + "/services/dataVirtDownload?vdbname="+Constants.SERVICE_VDB_PREFIX+serviceName; //$NON-NLS-1$
       	Window.Location.assign(contentUrl);
    }
     	
    private String getWebContext() {
        String context = GWT.getModuleBaseURL().replace( GWT.getModuleName() + "/", "" );
        if ( context.endsWith( "/" ) ) {
            context = context.substring( 0, context.length() - 1 );
        }
        return context;
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
     * Event handler that fires when the user clicks the CreateService button.
     * @param event
     */
    @EventHandler("btn-create-service")
    public void onCreateServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    /**
     * Create Service - transitions to the Create Services page
     */
    protected void doCreateService() {
    	placeManager.goTo("CreateDataServiceScreen");
    }
    
}
